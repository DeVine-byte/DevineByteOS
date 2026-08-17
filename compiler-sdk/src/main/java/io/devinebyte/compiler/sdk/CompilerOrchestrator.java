package io.devinebyte.compiler.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.devinebyte.compiler.dsl.KeywordDictionary;
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

import io.devinebyte.compiler.reporting.phase.ReportingPhase;
import io.devinebyte.compiler.reporting.collector.ReportCollector;
import io.devinebyte.compiler.reporting.collector.DependencyGraphBuilder;
import io.devinebyte.compiler.reporting.writer.JsonReportWriter;
import io.devinebyte.compiler.reporting.model.CompilationReport;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompilerOrchestrator {

    private final KeywordDictionary keywordDictionary = new KeywordDictionary();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Path compile(Path dslFile, String tenantId, String version, Path outputDir, boolean strictMode) throws Exception {
        String dslSource = Files.readString(dslFile);
        DiagnosticCollector diagnostics = new DiagnosticCollector();

        Map<String, String> keywordAliases = loadKeywordAliasesForTenant(tenantId);

        TenantContext tenant = new TenantContext(tenantId, TenantLifecycle.ACTIVE, Set.of());
        CompilationContext context = new CompilationContext(tenant, diagnostics);

        context.put("keywordAliases", keywordAliases);
        context.put("sourceCode", dslSource);
        context.put("dslPath", dslFile);
        context.put("outputDir", outputDir);
        context.put("version", version);
        context.put("strictMode", strictMode);

        var lexer = new Lexer(keywordDictionary);
        var tokens = lexer.scanTokens(context);
        var parser = new Parser(tokens);
        var ast = parser.parse(context);

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

        tenant = new TenantContext(tenantId, TenantLifecycle.ACTIVE, enabledModules);
        context = new CompilationContext(tenant, diagnostics);
        context.put("keywordAliases", keywordAliases);
        context.put("sourceCode", dslSource);
        context.put("dslPath", dslFile);
        context.put("outputDir", outputDir);
        context.put("version", version);
        context.put("ast", ast);
        context.put("moduleGraph", new ModuleGraph(moduleDefs));
        context.put("strictMode", strictMode);

        ReportCollector collector = new ReportCollector(new DependencyGraphBuilder());
        JsonReportWriter writer = new JsonReportWriter();

        collector.startPhase("semantic");
        new SemanticAnalyzer().analyze(context, ast);
        collector.endPhase("semantic",!context.diagnostics().hasErrors());

        collector.startPhase("audit");
        var emptyAudit = new AuditModel(dslSource, version, tenantId, tenant.state(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), new HashMap<>());
        var gaps = new GapAnalyzer().analyze(context, emptyAudit);
        var risks = new RiskAnalyzer().analyze(context, emptyAudit);
        var recs = new RecommendationGenerator().generate(context, emptyAudit, gaps, risks);
        var auditModel = new AuditModel(dslSource, version, tenantId, tenant.state(), List.of(), List.of(), List.of(), gaps, risks, recs, Map.of("file", dslFile.toString()));
        context.put("audit", auditModel);
        collector.endPhase("audit",!context.diagnostics().hasErrors());

        collector.startPhase("blueprint");
        CompilerResult<BlueprintIR> blueprintResult = new BlueprintCompiler(
            new ModuleCompiler(), new ModuleTreeShaker(), new ContractViolationEngine(), new AuditToBlueprintMapper()
        ).execute(context, null);
        BlueprintIR blueprint = blueprintResult.output();
        context.put("blueprint", blueprint);
        collector.endPhase("blueprint",!context.diagnostics().hasErrors());

        collector.startPhase("contracts");
        new ContractsPhase(new EventSchemaGenerator(), new EntitySchemaGenerator(), new WorkflowSchemaGenerator(), new APISchemaGenerator())
           .execute(context, blueprintResult);
        collector.endPhase("contracts",!context.diagnostics().hasErrors());

        collector.startPhase("workflow");
        new WorkflowCompiler().compile(tenant, context, blueprint);
        collector.endPhase("workflow",!context.diagnostics().hasErrors());

        collector.startPhase("projection");
        new ProjectionCompiler(new WasmGenerator()).compile(tenant, context, blueprint);
        collector.endPhase("projection",!context.diagnostics().hasErrors());

        collector.startPhase("generator");
        new GeneratorPhase(new DomainGenerator(), new RuntimeConfigGenerator(), new RuntimeBootstrapGenerator())
           .execute(context, blueprintResult);
        collector.endPhase("generator",!context.diagnostics().hasErrors());

        collector.startPhase("reporting");
        new ReportingPhase(collector).execute(context, blueprintResult);
        collector.endPhase("reporting",!context.diagnostics().hasErrors());

        collector.startPhase("packaging");
        CompilerResult<Path> packagingResult = new PackagingPhase(new PackageBuilder())
           .execute(context, blueprintResult);

        Path dbpkg = packagingResult.output();

        addManifestToZip(dbpkg, tenantId, version, keywordAliases);

        Path runtimeDir = Paths.get("build/generated/runtime");
        if (Files.exists(runtimeDir)) {
            addRuntimeToZip(dbpkg, runtimeDir);
        }

        collector.endPhase("packaging",!context.diagnostics().hasErrors());

        if (diagnostics.hasErrors()) {
            diagnostics.getDiagnostics().forEach(System.err::println);
            throw new RuntimeException("Compilation completed with errors.");
        }

        CompilationReport report = collector.build(context, blueprintResult, true, dbpkg.toString());
        Path reportPath = writer.write(context, report);

        return dbpkg;
    }

    private void addManifestToZip(Path zipFile, String tenantId, String version, Map<String, String> keywordAliases) throws IOException {
        // BUILD MANIFEST FROM SCRATCH - all 9 fields ManifestReader needs
        Map<String, Object> manifest = new HashMap<>();

        manifest.put("schemaVersion", "1.0"); // 1
        manifest.put("tenantId", tenantId); // 2
        manifest.put("version", version); // 3
        manifest.put("builtAt", Instant.now().toString()); // 4
        manifest.put("builtBy", "devine-byte-compiler"); // 5
        manifest.put("sha256", "TBD"); // 6 placeholder for pass 1
        manifest.put("signature", "dev"); // 7
        manifest.put("multiTenant", true); // 8
        manifest.put("keywordAliases", keywordAliases); // 9

        Map<String, String> env = Map.of("create", "true");

        // PASS 1: write with TBD
        try (FileSystem fs = FileSystems.newFileSystem(zipFile, env)) {
            Files.writeString(fs.getPath("/manifest.json"), objectMapper.writeValueAsString(manifest));
        }

        // PASS 2: compute real sha256 and overwrite
        String sha256 = computeSha256(zipFile);
        manifest.put("sha256", sha256);
        try (FileSystem fs = FileSystems.newFileSystem(zipFile, env)) {
            Files.writeString(fs.getPath("/manifest.json"), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest));
        }
    }

    private String computeSha256(Path path) throws IOException {
        try (var in = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) digest.update(buf, 0, n);
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("Failed to compute sha256", e);
        }
    }

    private void addRuntimeToZip(Path zipFile, Path runtimeDir) throws IOException {
        Map<String, String> env = Map.of("create", "true");
        try (FileSystem fs = FileSystems.newFileSystem(zipFile, env)) {
            Files.walk(runtimeDir).filter(Files::isRegularFile).forEach(path -> {
                try {
                    Path target = fs.getPath("/runtime/" + runtimeDir.relativize(path).toString().replace("\\", "/"));
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public Path compile(Path dslFile, String tenantId, String version, Path outputDir) throws Exception {
        return compile(dslFile, tenantId, version, outputDir, false);
    }

    private Map<String, String> loadKeywordAliasesForTenant(String tenantId) {
        Path aliasPath = Paths.get("tenants", tenantId, "aliases.json");
        if (!Files.exists(aliasPath)) {
            return Map.of();
        }
        try {
            String json = Files.readString(aliasPath);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            System.err.println("[WARN] Failed to load aliases for " + tenantId + ": " + e.getMessage());
            return Map.of();
        }
    }
}
