package io.devinebyte.compiler.packaging.phase;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.contracts.model.EventSchema;
import io.devinebyte.compiler.contracts.model.EntitySchema;
import io.devinebyte.compiler.contracts.model.WorkflowSchema;    
import io.devinebyte.compiler.core.context.CompilationContext;   
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.packaging.builder.PackageBuilder;  
import io.devinebyte.compiler.packaging.model.PackageContent;
import io.devinebyte.compiler.projection.model.DashboardDefinition;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import io.devinebyte.compiler.workflow.model.ExecutableStateMachine;                                                              
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter;
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema;                                                            
import io.devinebyte.runtime.config.ModuleGraph;                 
import jakarta.inject.Inject;
import jakarta.inject.Singleton;                                 
import java.io.IOException;
import java.nio.file.Files;                                      
import java.nio.file.Path;
import java.util.HashSet;                                        
import java.util.List;                                           
import java.util.Map;
import java.util.Set;                                            
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper; // FIX: Added import

@Singleton
public class PackagingPhase implements CompilerPhase {
    private final PackageBuilder builder;
    private final ObjectMapper mapper = new ObjectMapper(); // FIX: Added mapper

    @Inject
    public PackagingPhase(PackageBuilder builder) {
        this.builder = builder;
    }

    @Override
    public String name() { return "packaging"; }

    @Override
    public CompilerResult<Path> execute(CompilationContext context, CompilerResult previous) {
        BlueprintIR ir = (BlueprintIR) previous.output();

        List<EventSchema> events = context.get("eventSchemas");
        List<EntitySchema> entities = context.get("entitySchemas");
        List<WorkflowSchema> workflowSchemas = context.get("workflowSchemas");
        List<ApiSchema> apis = ir.apiSchemas();
        
        // FIX: Instead of relying purely on context memory, read all generated workflows dynamically from disk
        List<ExecutableStateMachine> workflows = new java.util.ArrayList<>();
        try {
            Path wfDir = Path.of("build/workflows");
            if (Files.exists(wfDir)) {
                try (var stream = Files.list(wfDir)) {
                    List<Path> wfFiles = stream
                        .filter(p -> p.toString().endsWith(".json") && !p.getFileName().toString().equals("compiled_state_machines.json"))
                        .collect(Collectors.toList());
                        
                    for (Path path : wfFiles) {
                        try {
                            ExecutableStateMachine sm = mapper.readValue(path.toFile(), ExecutableStateMachine.class);
                            workflows.add(sm);
                        } catch (Exception ex) {
                            System.err.println("[PKG WARNING] Skipping unparseable workflow file: " + path.getFileName() + " (" + ex.getMessage() + ")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            context.diagnostics().addError("PKG_WF_LOAD_ERR", "Failed to load generated workflows from disk: " + e.getMessage());
        }

        // Fallback to memory context if disk sweep yielded nothing
        if (workflows.isEmpty()) {
            List<ExecutableStateMachine> ctxWfs = context.get("workflows");
            if (ctxWfs != null) workflows.addAll(ctxWfs);
        }

        List<ProjectionFunction> projections = context.get("projections");
        List<DashboardDefinition> dashboards = context.get("dashboards");
        byte[] bootstrap = context.get("runtimeBootstrap");
        Map<String, String> config = context.get("tenantConfig");
        Map<String, Boolean> flags = context.get("featureFlags");
        ModuleGraph graph = context.get("moduleGraph");

        Path contextPluginsDir = context.get("pluginsDir");
        if (contextPluginsDir == null) {
            contextPluginsDir = Path.of("plugins");
        }

        validateNoCycles(graph, context);

        Boolean strictModeFlag = context.get("strictMode");
        boolean strictMode = strictModeFlag != null && strictModeFlag;
        boolean multiTenant = !strictMode;
        System.out.println("PKG DEBUG: apiSchemas size = " + (apis == null ? 0 : apis.size()));
        System.out.println("PKG DEBUG: Verified packaged workflows count = " + workflows.size());

        PackageContent pkgContent = new PackageContent(
            context.tenant(), ir.version(), ir,
            events != null ? events : List.of(), entities != null ? entities : List.of(),
            workflowSchemas != null ? workflowSchemas : List.of(), apis != null ? apis : List.of(),
            workflows, projections != null ? projections : List.of(), // Passed fixed disk workflows list
            dashboards != null ? dashboards : List.of(), bootstrap != null ? bootstrap : new byte[0],
            config != null ? config : Map.of(), flags != null ? flags : Map.of(), graph,
            multiTenant, contextPluginsDir
        );

        try {
            Path baseOutputDir = context.get("outputDir");
            if (baseOutputDir == null) {
                baseOutputDir = Path.of("execution");
            }
            Path outputDir = baseOutputDir.resolve(context.tenant().tenantId());
            Files.createDirectories(outputDir);

            Path dbpkg = builder.build(pkgContent, outputDir);

            context.diagnostics().addInfo("PKG_001", "Package built: " + dbpkg.toAbsolutePath());
            return new CompilerResult<>(context.tenant(), context.diagnostics(), dbpkg);

        } catch (Exception e) {
            context.diagnostics().addError("PKG_500", "Packaging failed: " + e.getMessage());
            return new CompilerResult<>(context.tenant(), context.diagnostics(), null);
        }
    }

    private void validateNoCycles(ModuleGraph graph, CompilationContext context) {
        if (graph == null) return;
        Map<String, Set<String>> deps = graph.modules().entrySet().stream()
         .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().dependsOn()));
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        for (String module : deps.keySet()) {
            if (hasCycle(module, deps, visited, recStack, context)) {
                throw new IllegalStateException("Cyclic dependency detected involving module: " + module);
            }
        }
        context.diagnostics().addInfo("PKG_003", "Dependency validation: No cycles found");
    }

    private boolean hasCycle(String node, Map<String, Set<String>> deps, Set<String> visited, Set<String> recStack, CompilationContext context) {
        if (recStack.contains(node)) {
            context.diagnostics().addError("PKG_002", "Cyclic dependency: " + node);
            return true;
        }
        if (visited.contains(node)) return false;
        visited.add(node);
        recStack.add(node);
        for (String dep : deps.getOrDefault(node, Set.of())) {
            if (hasCycle(dep, deps, visited, recStack, context)) return true;
        }
        recStack.remove(node);
        return false;
    }
}

