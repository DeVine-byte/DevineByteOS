package org.devinebyte.compiler.parser.ast;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends AstNode {

    private final List<AstNode> declarations = new ArrayList<>();

    public List<AstNode> getDeclarations() {
        return declarations;
    }

}
