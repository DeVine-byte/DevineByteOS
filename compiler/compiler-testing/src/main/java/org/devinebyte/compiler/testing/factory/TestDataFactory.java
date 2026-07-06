package org.devinebyte.compiler.testing.factory;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static String entityDsl() {

        return """
               entity Customer {

                   id UUID

                   name STRING

               }
               """;

    }

}
