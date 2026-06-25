package org.devinebyte.compiler.parser.parser;

import com.devinebyte.compiler.parser.ast.ProgramNode;

public interface Parser {

    ProgramNode parse(String source);

}
