package org.devinebyte.compiler.parser.parser;

import org.devinebyte.compiler.parser.ast.ProgramNode;

public interface Parser {

    ProgramNode parse(String source);

}
