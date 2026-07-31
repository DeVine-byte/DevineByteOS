package io.devinebyte.compiler.contracts.generator;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.contracts.model.EntitySchema;
import io.devinebyte.compiler.contracts.model.Field;
import io.devinebyte.compiler.contracts.model.FieldType;
import io.devinebyte.compiler.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class EntitySchemaGenerator {
    
    public List<EntitySchema> generate(TenantContext tenant, BlueprintIR ir) {
        return ir.entities().stream()
            .map(e -> new EntitySchema(
                e.name(),
                "id",
                List.of(new Field("id", FieldType.UUID, true))
            ))
            .toList();
    }
}
