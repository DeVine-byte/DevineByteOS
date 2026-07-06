package org.devinebyte.compiler.parser.ast;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public class PropertyNode extends AstNode {

    private String name;

    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
