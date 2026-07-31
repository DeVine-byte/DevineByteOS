package io.devinebyte.compiler.generator.codegen;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.contracts.model.*;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.generator.model.GenerationResult;
import io.devinebyte.runtime.config.ModuleGraph; // ADD
import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class DomainGenerator {
    private final AggregateGenerator aggregateGenerator = new AggregateGenerator();
    private final ServiceGenerator serviceGenerator = new ServiceGenerator();

    public GenerationResult generate(TenantContext tenant, BlueprintIR ir, Path outDir) throws IOException {
        List<EventSchema> events = new ArrayList<>();
        List<EntitySchema> entities = new ArrayList<>();

        for (var module : ir.modules()) {
            if (!tenant.enabledModules().contains(module.name())) continue;

            Path moduleDir = outDir.resolve("domain").resolve(module.name());
            Files.createDirectories(moduleDir);

            for (var entity : ir.entities()) {
                if (!entity.moduleId().equals(module.name())) continue;

                String code = aggregateGenerator.generate(tenant, entity);
                Files.writeString(moduleDir.resolve(entity.name() + "Aggregate.java"), code);

                List<Field> fields = entity.fields().entrySet().stream()
                    .map(e -> new Field(e.getKey(), toFieldType(e.getValue()), false))
                    .toList();

                entities.add(new EntitySchema(entity.name(), module.name(), fields));
            }
        }

        // FIX: Pass empty ModuleGraph here. RuntimeConfigGenerator will overwrite it with real one
        return new GenerationResult(
            events, 
            entities, 
            List.of(), // workflowSchemas
            List.of(), // apiSchemas
            List.of(), // workflows
            List.of(), // projections
            List.of(), // dashboards
            Map.of(),  // config
            Map.of(),  // featureFlags
            new ModuleGraph(Map.of()) // ADD THIS
        );
    }

    private FieldType toFieldType(String raw) {
        return switch (raw.toLowerCase()) {
            case "string" -> FieldType.STRING;
            case "uuid" -> FieldType.UUID;
            case "long", "int" -> FieldType.LONG;
            case "decimal", "double", "float" -> FieldType.DECIMAL;
            case "boolean", "bool" -> FieldType.BOOLEAN;
            case "datetime", "date", "timestamp" -> FieldType.DATETIME;
            case "json", "object" -> FieldType.JSON;
            default -> FieldType.STRING;
        };
    }
}
