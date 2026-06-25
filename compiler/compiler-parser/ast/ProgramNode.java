package com.devinebyte.compiler.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends AstNode {

    private final List<AstNode> declarations = new ArrayList<>();

    public List<AstNode> getDeclarations() {
        return declarations;
    }

}
