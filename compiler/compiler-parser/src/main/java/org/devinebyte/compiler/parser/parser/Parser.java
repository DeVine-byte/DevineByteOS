package org.devinebyte.compiler.parser.parser;

import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.parser.lexer.Token;

import java.util.List;

public interface Parser {

    ProgramNode parse(List<Token> tokens);

}
