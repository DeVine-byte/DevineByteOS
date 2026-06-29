package org.devinebyte.compiler.testing.factory;

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
