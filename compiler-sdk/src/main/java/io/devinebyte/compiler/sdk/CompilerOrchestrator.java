package io.devinebyte.compiler.sdk;

import io.devinebyte.compiler.audit.analyzer.GapAnalyzer;
import io.devinebyte.compiler.audit.analyzer.RiskAnalyzer;
import io.devinebyte.compiler.audit.analyzer.RecommendationGenerator;
import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.blueprint.compiler.BlueprintCompiler;
import io.devinebyte.compiler.blueprint.compiler.ModuleCompiler;
import io.devinebyte.compiler.blueprint.mapper.AuditToBlueprintMapper;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.shake.ModuleTreeShaker;
import io.devinebyte.compiler.blueprint.validation.ContractViolationEngine;
import io.devinebyte.compiler.contracts.phase.ContractsPhase;
import io.devinebyte.compiler.contracts.generator.APISchemaGenerator;
import io.devinebyte.compiler.contracts.generator.EntitySchemaGenerator;
import io.devinebyte.compiler.contracts.generator.EventSchemaGenerator;
import io.devinebyte.compiler.contracts.generator.WorkflowSchemaGenerator;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.core.context.TenantLifecycle;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.dsl.lexer.Lexer;
import io.devinebyte.compiler.dsl.parser.Parser;
import io.devinebyte.compiler.dsl.ast.ModuleNode;
import io.devinebyte.compiler.dsl.semantic.SemanticAnalyzer;
import io.devinebyte.compiler.generator.phase.GeneratorPhase;
import io.devinebyte.compiler.generator.codegen.DomainGenerator;
import io.devinebyte.compiler.generator.runtime.RuntimeBootstrapGenerator;
import io.devinebyte.compiler.generator.runtime.RuntimeConfigGenerator;
import io.devinebyte.compiler.packaging.builder.PackageBuilder;
import io.devinebyte.compiler.packaging.phase.PackagingPhase;
import io.devinebyte.compiler.projection.compiler.ProjectionCompiler;
import io.devinebyte.compiler.projection.compiler.WasmGenerator;
import io.devinebyte.compiler.workflow.compiler.WorkflowCompiler;
import io.devinebyte.runtime.config.ModuleGraph;

// Reporting
import io.devinebyte.compiler.reporting.phase.ReportingPhase;
import io.devinebyte.compiler.reporting.collector.ReportCollector;
import io.devinebyte.compiler.reporting.collector.DependencyGraphBuilder;
import io.devinebyte.compiler.reporting.writer.JsonReportWriter;
import io.devinebyte.compiler.reporting.model.CompilationReport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompilerOrchestrator {

    // NEW: 5-arg method with strictMode
    public Path compile(Path dslFile, String tenantId, String version, Path outputDir, boolean strictMode) throws Exception {
        String dslSource = Files.readString(dslFile);
        DiagnosticCollector diagnostics = new DiagnosticCollector();

        // 1. DSL -> Lexer -> Parser first to get modules
        var lexer = new Lexer(dslSource);
        var tokens = lexer.scanTokens(new CompilationContext(null, diagnostics));
        var parser = new Parser(tokens);
        var ast = parser.parse(new CompilationContext(null, diagnostics));

        Map<String, ModuleGraph.ModuleDefinition> moduleDefs = ast.stream()
           .filter(n -> n instanceof ModuleNode)
           .map(n -> (ModuleNode) n)
           .collect(Collectors.toMap(
                ModuleNode::name,
                m -> new ModuleGraph.ModuleDefinition(
                    m.name(),
                    m.enabled(),
                    m.dependencies(),
                    Set.of(),
                    Set.of()
                )
            ));

        Set<String> enabledModules = moduleDefs.entrySet().stream()
           .filter(e -> e.getValue().enabled())
           .map(Map.Entry::getKey)
           .collect(Collectors.toSet());

        TenantContext tenant = new TenantContext(tenantId, TenantLifecycle.ACTIVE, enabledModules);
        CompilationContext context = new CompilationContext(tenant, diagnostics);

        // Put immutable inputs in context
        context.put("dslSource", dslSource);
        context.put("dslPath", dslFile);
        context.put("outputDir", outputDir);
        context.put("version", version);
        context.put("ast", ast);
        context.put("moduleGraph", new ModuleGraph(moduleDefs));
        context.put("strictMode", strictMode); // NEW: Pass to PackagingPhase

        ReportCollector collector = new ReportCollector(new DependencyGraphBuilder());
        JsonReportWriter writer = new JsonReportWriter();

        System.out.println("=== FILE CONTENT ===\n" + dslSource + "\n==================");
        System.out.println("=== TOKEN DUMP ===");
        for (var token : tokens) {
            System.out.printf("%-12s '%s' line:%d col:%d%n", token.type(), token.lexeme(), token.line(), token.column());
        }
        System.out.println("==================");
        System.out.println("=== AST DUMP ===");
        System.out.println(ast);
        System.out.println("Root AST nodes count: " + ast.size());
        System.out.println("================");

        // 3. Semantic
        collector.startPhase("semantic");
        new SemanticAnalyzer().analyze(context, ast);
        collector.endPhase("semantic",!context.diagnostics().hasErrors());

        // 4. Audit
        collector.startPhase("audit");
        var emptyAudit = new AuditModel(dslSource, version, tenantId, tenant.state(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), new HashMap<>());
        var gaps = new GapAnalyzer().analyze(context, emptyAudit);
        var risks = new RiskAnalyzer().analyze(context, emptyAudit);
        var recs = new RecommendationGenerator().generate(context, emptyAudit, gaps, risks);
        var auditModel = new AuditModel(dslSource, version, tenantId, tenant.state(), List.of(), List.of(), List.of(), gaps, risks, recs, Map.of("file", dslFile.toString()));
        context.put("audit", auditModel);
        collector.endPhase("audit",!context.diagnostics().hasErrors());

        // 5. Blueprint
        collector.startPhase("blueprint");
        CompilerResult<BlueprintIR> blueprintResult = new BlueprintCompiler(
            new ModuleCompiler(), new ModuleTreeShaker(), new ContractViolationEngine(), new AuditToBlueprintMapper()
        ).execute(context, null);
        BlueprintIR blueprint = blueprintResult.output();
        context.put("blueprint", blueprint);
        collector.endPhase("blueprint",!context.diagnostics().hasErrors());

        // 6. Contracts
        collector.startPhase("contracts");
        new ContractsPhase(new EventSchemaGenerator(), new EntitySchemaGenerator(), new WorkflowSchemaGenerator(), new APISchemaGenerator())
           .execute(context, blueprintResult);
        collector.endPhase("contracts",!context.diagnostics().hasErrors());

        // 7. Workflow
        collector.startPhase("workflow");
        new WorkflowCompiler().compile(tenant, context, blueprint);
        collector.endPhase("workflow",!context.diagnostics().hasErrors());

        // 8. Projection
        collector.startPhase("projection");
        new ProjectionCompiler(new WasmGenerator()).compile(tenant, context, blueprint);
        collector.endPhase("projection",!context.diagnostics().hasErrors());

        // 9. Generator
        collector.startPhase("generator");
        new GeneratorPhase(new DomainGenerator(), new RuntimeConfigGenerator(), new RuntimeBootstrapGenerator())
           .execute(context, blueprintResult);
        collector.endPhase("generator",!context.diagnostics().hasErrors());

        // 10. Reporting
        collector.startPhase("reporting");
        new ReportingPhase(collector).execute(context, blueprintResult);
        collector.endPhase("reporting",!context.diagnostics().hasErrors());

        // 11. Packaging
        collector.startPhase("packaging");
        CompilerResult<Path> packagingResult = new PackagingPhase(new PackageBuilder())
           .execute(context, blueprintResult);
        collector.endPhase("packaging",!context.diagnostics().hasErrors());

        if (diagnostics.hasErrors()) {
            System.err.println("=== COMPILATION ERRORS DETECTED ===");
            diagnostics.getDiagnostics().forEach(System.err::println);
            throw new RuntimeException("Compilation completed with errors.");
        }

        CompilationReport report = collector.build(context, blueprintResult, true, packagingResult.output().toString());
        Path reportPath = writer.write(context, report);

        System.out.println("[DBPKG] " + packagingResult.output());
        System.out.println("[REPORT] " + reportPath);
        return packagingResult.output();
    }

    // OLD: Keep for backwards compat. Defaults to template mode
    public Path compile(Path dslFile, String tenantId, String version, Path outputDir) throws Exception {
        return compile(dslFile, tenantId, version, outputDir, false);
    }
}
