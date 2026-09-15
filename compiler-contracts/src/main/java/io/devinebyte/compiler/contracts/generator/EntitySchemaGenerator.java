package io.devinebyte.compiler.contracts.generator;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.contracts.model.EntitySchema;
import io.devinebyte.compiler.contracts.model.Field;
import io.devinebyte.compiler.contracts.model.FieldType;
import io.devinebyte.compiler.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EntitySchemaGenerator {

    public List<EntitySchema> generate(TenantContext tenant, BlueprintIR ir) {
        return ir.entities().stream()
            .map(e -> {
                List<Field> entityFields = new ArrayList<>();
                // Enforce baseline operational tracking key identifiers
                entityFields.add(new Field("id", FieldType.UUID, true));
                
                // Dynamically populate actual schema elements derived from active domain fields
                if (e.fields() != null) {
                    e.fields().forEach((fieldName, typeStr) -> 
                        entityFields.add(new Field(fieldName, toFieldType(typeStr), true))
                    );
                }
                
                return new EntitySchema(e.name(), "id", entityFields);
            })
            .toList();
    }

    private FieldType toFieldType(String typeStr) {
        return switch (typeStr.toLowerCase()) {
            case "string" -> FieldType.STRING;
            case "uuid" -> FieldType.UUID;
            case "long", "int" -> FieldType.LONG;
            case "decimal", "double", "float" -> FieldType.DECIMAL;
            case "boolean", "bool" -> FieldType.BOOLEAN;
            case "datetime", "date", "time" -> FieldType.DATETIME;
            case "json", "map" -> FieldType.JSON;
            default -> FieldType.STRING;
        };
    }
}

