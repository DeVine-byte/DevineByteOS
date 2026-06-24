package compiler.dsl.ast;

import java.util.Map;

public class EntityNode implements AstNode {

    private final String name;
    private final Map<String, String> fields;

    public EntityNode(String name, Map<String, String> fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}
