package org.devinebyte.compiler.parser.lexer;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.util.List;

public interface Lexer {

    List<Token> tokenize(String source);

}
