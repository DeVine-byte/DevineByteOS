package io.devinebyte.compiler.contracts.generator;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EventIR;
import io.devinebyte.compiler.contracts.model.EventSchema;
import io.devinebyte.compiler.contracts.model.FieldType;
import io.devinebyte.compiler.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class EventSchemaGenerator {
    
    public List<EventSchema> generate(TenantContext tenant, BlueprintIR ir) {
        return ir.events().stream()
            .map(e -> new EventSchema(
                e.name(),
                e.version(), // use event version, not IR version
                buildFields(e)
            ))
            .toList();
    }

    private Map<String, FieldType> buildFields(EventIR e) {
        Map<String, FieldType> fields = new HashMap<>();
        
        // Standard envelope per Rule 1: Event Sourced
        fields.put("tenantId", FieldType.STRING);
        fields.put("eventId", FieldType.UUID);
        fields.put("timestamp", FieldType.DATETIME);
        fields.put("version", FieldType.STRING);
        
        // Payload from DSL -> Blueprint
        if (e.payload() != null) {
            e.payload().forEach((key, typeStr) -> fields.put(key, toFieldType(typeStr)));
        }
        
        return fields;
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
            default -> FieldType.STRING; // safe default
        };
    }
}
