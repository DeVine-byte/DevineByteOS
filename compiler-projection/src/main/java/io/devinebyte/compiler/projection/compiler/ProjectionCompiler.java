package io.devinebyte.compiler.projection.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EventIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.projection.model.DashboardDefinition;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import io.devinebyte.compiler.projection.model.WidgetDefinition;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

@Singleton
public class ProjectionCompiler {

    private final WasmGenerator wasmGenerator;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Inject
    public ProjectionCompiler(WasmGenerator wasmGenerator) {
        this.wasmGenerator = wasmGenerator;
    }

    public void compile(TenantContext tenant, CompilationContext context, BlueprintIR ir) {
        if (!tenant.enabledModules().contains("dashboard")) {
            context.diagnostics().addInfo("PROJECTION", "Dashboard module disabled for tenant " + tenant.tenantId() + ". Skipping.");
            return; // Rule 4: Module Isolation
        }
        
        context.diagnostics().addInfo("PROJECTION", "Compiling projections for tenant " + tenant.tenantId());

        List<ProjectionFunction> functions = new ArrayList<>();
        List<DashboardDefinition> dashboards = new ArrayList<>();

        for (ModuleIR module : ir.modules()) {
            if (!tenant.enabledModules().contains(module.name())) continue; // Rule 4

            for (EventIR event : ir.events()) {
                String wasm = wasmGenerator.generateWasm(event.name(), module.name());
                functions.add(new ProjectionFunction(
                    module.name() + "_" + event.name() + "_Projection",
                    event.name(),
                    Base64.getEncoder().encodeToString(wasm.getBytes())
                ));
            }
            
            // Generate default dashboard per module
            dashboards.add(new DashboardDefinition(
                module.name() + "_Dashboard",
                module.name() + " Overview",
                module.name(),
                List.of(new WidgetDefinition(
                    "kpi_1", "kpi", "Total Events", 
                    module.name() + "_Projection", 
                    "{\"agg\":\"count\"}"
                ))
            ));
        }

        try {
            Path outDir = Path.of("build/projections");
            Files.createDirectories(outDir);
            Files.writeString(outDir.resolve("projection_functions.wasm"), serializeWasmBundle(functions));
            mapper.writeValue(outDir.resolve("dashboard_definitions.json").toFile(), dashboards);
            context.diagnostics().addInfo("PROJECTION", "Wrote " + functions.size() + " functions and " + dashboards.size() + " dashboards");
        } catch (Exception e) {
            context.diagnostics().addError("PROJECTION_IO", "Failed to write projections: " + e.getMessage());
        }
    }
    
    private String serializeWasmBundle(List<ProjectionFunction> functions) {
        // In prod this is a real wasm module. For now JSON array of functions
        try { return mapper.writeValueAsString(functions); } 
        catch (Exception e) { return "[]"; }
    }
}
