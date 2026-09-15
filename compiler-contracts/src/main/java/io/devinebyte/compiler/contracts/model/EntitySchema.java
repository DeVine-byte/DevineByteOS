package io.devinebyte.compiler.contracts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EntitySchema(
    String name,
    String key,
    List<Field> fields
) {
    @JsonProperty("$schema")
    public String getJsonSchemaStandard() {
        return "https://json-schema.org";
    }

    @JsonProperty("$id")
    public String getJsonSchemaId() {
        return "https://devinebyte.io" + name + ".json";
    }

    @JsonProperty("type")
    public String getJsonSchemaType() {
        return "object";
    }

    @JsonProperty("properties")
    public Map<String, Object> getJsonSchemaProperties() {
        Map<String, Object> props = new LinkedHashMap<>();
        if (fields != null) {
            for (Field f : fields) {
                props.put(f.name(), mapTypeToConstraints(f.type()));
            }
        }
        return props;
    }

    @JsonProperty("required")
    public List<String> getJsonSchemaRequired() {
        List<String> required = new ArrayList<>();
        if (fields != null) {
            for (Field f : fields) {
                if (f.required()) {
                    required.add(f.name());
                }
            }
        }
        return required;
    }

    @JsonProperty("additionalProperties")
    public boolean getAdditionalPropertiesConstraint() {
        return false;
    }

    private Map<String, Object> mapTypeToConstraints(FieldType type) {
        Map<String, Object> map = new LinkedHashMap<>();
        switch (type) {
            case UUID -> {
                map.put("type", "string");
                map.put("format", "uuid");
            }
            case DATETIME -> {
                map.put("type", "string");
                map.put("format", "date-time");
            }
            case LONG -> map.put("type", "integer");
            case DECIMAL -> map.put("type", "number");
            case BOOLEAN -> map.put("type", "boolean");
            case JSON -> map.put("type", "object");
            case STRING -> map.put("type", "string");
            default -> map.put("type", "string");
        }
        return map;
    }
}

