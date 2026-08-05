package io.devinebyte.runtime.projection.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import io.devinebyte.runtime.projection.model.ProjectionResult;
import io.devinebyte.compiler.projection.model.DashboardDefinition;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@jakarta.inject.Singleton
public final class ProjectionLoader {
    private final ObjectMapper mapper;

    @jakarta.inject.Inject
    public ProjectionLoader(ObjectMapper mapper) { this.mapper = mapper; }

    public ProjectionLoadResult load(FileSystem fs, DiagnosticCollector diagnostics) throws Exception {
        Path wasmPath = fs.getPath("projections/projection_functions.wasm");
        Path dashPath = fs.getPath("projections/dashboard_definitions.json");

        if (!Files.exists(wasmPath) || !Files.exists(dashPath)) {
            diagnostics.add("DBRT600", DiagnosticSeverity.FATAL, "Missing /projections files in dbpkg");
            return new ProjectionLoadResult(List.of(), List.of(), diagnostics);
        }

        byte[] wasmBytes = Files.readAllBytes(wasmPath);
        try (InputStream in = Files.newInputStream(dashPath)) {
            List<DashboardDefinition> dashboards = mapper.readValue(in, 
                mapper.getTypeFactory().constructCollectionType(List.class, DashboardDefinition.class));
            
            List<ProjectionFunction> functions = dashboards.stream()
                .flatMap(d -> d.functions().stream())
                .toList();
            
            return new ProjectionLoadResult(functions, dashboards, diagnostics);
        }
    }
}
