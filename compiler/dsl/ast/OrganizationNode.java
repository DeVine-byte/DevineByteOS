package compiler.dsl.ast;

import java.util.List;

public class OrganizationNode implements AstNode {

    private final String name;
    private final List<AstNode> children;

    public OrganizationNode(String name, List<AstNode> children) {
        this.name = name;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public List<AstNode> getChildren() {
        return children;
    }
}
