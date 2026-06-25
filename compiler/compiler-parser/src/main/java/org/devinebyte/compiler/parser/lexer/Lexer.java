package org.devinebyte.compiler.parser.lexer;

import java.util.List;

public interface Lexer {

    List<Token> tokenize(String source);

}
