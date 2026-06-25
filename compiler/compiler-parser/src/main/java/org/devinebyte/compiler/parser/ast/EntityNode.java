package org.devinebyte.compiler.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class EntityNode extends AstNode {

    private String name;

    private final List<PropertyNode> properties = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PropertyNode> getProperties() {
        return properties;
    }
}
