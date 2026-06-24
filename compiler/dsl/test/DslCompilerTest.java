package compiler.dsl.test;

import compiler.dsl.lexer.DslLexer;
import compiler.dsl.parser.DslParser;
import compiler.dsl.semantic.SemanticAnalyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DslCompilerTest {

    @Test
    public void testDslCompilation() {

        String source = """
        organization "Acme Corp" {
            entity Customer {
                id: UUID
            }

            workflow Onboarding {
                state START
                state ACTIVE
                transition START -> ACTIVE
            }
        }
        """;

        var tokens = new DslLexer(source).tokenize();
        var ast = new DslParser(tokens).parse();

        assertNotNull(ast);

        new SemanticAnalyzer().validate(ast);
    }
}
