package org.devinebyte.compiler.parser.lexer;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import java.util.List;

public interface Lexer {

    List<Token> tokenize(String source);

}
