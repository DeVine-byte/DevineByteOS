package org.devinebyte.compiler.blueprint.model;

import java.util.ArrayList;
import java.util.List;

public class EntityDefinition {

    private String name;

    private final List<PropertyDefinition> properties = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PropertyDefinition> getProperties() {
        return properties;
    }

}
