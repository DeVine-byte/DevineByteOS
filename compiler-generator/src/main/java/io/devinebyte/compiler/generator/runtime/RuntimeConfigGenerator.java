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
import java.util.*;
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

        // FIX: Build modules with real deps from BlueprintIR + inject RUNTIME
        Map<String, ModuleDefinition> modules = new LinkedHashMap<>();
        
        // 1. Always inject system RUNTIME module
        modules.put("runtime", new ModuleDefinition(
            "runtime", true, Set.of(), Set.of(), Set.of()
        ));

        // 2. Add user modules with deps converted to lowercase
        for (ModuleIR m : ir.modules()) {
            if (!m.enabled()) continue;
            
            Set<String> deps = m.dependencies().stream()
                .map(String::toLowerCase) // sales, runtime
                .collect(Collectors.toSet());
                
            modules.put(m.id(), new ModuleDefinition(
                m.id(),
                m.enabled(),
                deps,
                m.events().stream().map(EventIR::name).collect(Collectors.toSet()),
                Set.of()
            ));
        }
        
        ModuleGraph moduleGraph = new ModuleGraph(modules);

        mapper.writeValue(outDir.resolve("tenant_config.json").toFile(), fileConfig);
        mapper.writeValue(outDir.resolve("feature_flags.json").toFile(), flags);
        mapper.writeValue(outDir.resolve("module_graph.json").toFile(), moduleGraph);

        System.out.println("[GENERATOR] Wrote module_graph.json with " + modules.size() + " modules");

        return new GenerationResult(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), resultConfig, flags, moduleGraph);
    }
}
