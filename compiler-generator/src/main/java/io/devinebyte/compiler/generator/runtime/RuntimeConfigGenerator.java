package io.devinebyte.compiler.generator.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EventIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.generator.model.GenerationResult;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import jakarta.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class RuntimeConfigGenerator {
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public GenerationResult generate(TenantContext tenant, BlueprintIR ir, Path outDir) throws Exception {
        Files.createDirectories(outDir);

        List<String> requestedModules = ir.modules().stream()
            .filter(ModuleIR::enabled)
            .map(m -> m.id().toLowerCase())
            .sorted()
            .collect(Collectors.toList());

        Map<String, Object> fileConfig = Map.of(
            "tenantId", tenant.tenantId(),
            "lifecycle", tenant.state().name(),
            "version", ir.version(),
            "requestedModules", requestedModules
        );

        Map<String, String> resultConfig = Map.of(
            "tenantId", tenant.tenantId(),
            "lifecycle", tenant.state().name(),
            "version", ir.version(),
            "requestedModules", String.join(",", requestedModules)
        );

        Map<String, Boolean> flags = ir.modules().stream()
            .collect(Collectors.toMap(ModuleIR::id, ModuleIR::enabled));

        Map<String, ModuleDefinition> modules = ir.modules().stream()
            .collect(Collectors.toMap(
                ModuleIR::id,
                m -> new ModuleDefinition(
                    m.id(),
                    m.enabled(),
                    Set.copyOf(m.dependencies()),
                    m.events().stream().map(EventIR::name).collect(Collectors.toSet()),
                    Set.of()
                )
            ));
        ModuleGraph moduleGraph = new ModuleGraph(modules);

        mapper.writeValue(outDir.resolve("tenant_config.json").toFile(), fileConfig);
        mapper.writeValue(outDir.resolve("feature_flags.json").toFile(), flags);
        mapper.writeValue(outDir.resolve("module_graph.json").toFile(), moduleGraph);

        return new GenerationResult(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), resultConfig, flags, moduleGraph);
    }
}
