package io.devinebyte.runtime.projection.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.Diagnostic;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import io.devinebyte.compiler.projection.model.DashboardDefinition;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@jakarta.inject.Singleton
public final class ProjectionLoader {
    private final ObjectMapper mapper;

    @jakarta.inject.Inject
    public ProjectionLoader(ObjectMapper mapper) { this.mapper = mapper; }

    public ProjectionLoadResult load(FileSystem fs, DiagnosticCollector diagnostics) throws Exception {
        Path funcPath = fs.getPath("projections/projection_functions.json");
        Path dashPath = fs.getPath("projections/dashboard_definitions.json");

        if (!Files.exists(funcPath) || !Files.exists(dashPath)) {
            diagnostics.add(new Diagnostic(
                "DBRT600", 
                DiagnosticSeverity.FATAL, 
                "Missing /projections files in dbpkg",
                "system",
                Instant.now()
            ));
            return new ProjectionLoadResult(List.of(), List.of(), diagnostics);
        }

        try (InputStream funcIn = Files.newInputStream(funcPath);
             InputStream dashIn = Files.newInputStream(dashPath)) {
            
            List<ProjectionFunction> functions = mapper.readValue(funcIn,
                mapper.getTypeFactory().constructCollectionType(List.class, ProjectionFunction.class));

            List<DashboardDefinition> dashboards = mapper.readValue(dashIn,
                mapper.getTypeFactory().constructCollectionType(List.class, DashboardDefinition.class));

            return new ProjectionLoadResult(functions, dashboards, diagnostics);
        }
    }
}
