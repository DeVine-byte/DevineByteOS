package org.devinebyte.compiler.testing.builders;

public final class BlueprintBuilder {

    private String name = "TestBlueprint";

    public BlueprintBuilder name(String name) {
        this.name = name;
        return this;
    }

    public String build() {

        return """
               blueprint %s {
               }
               """.formatted(name);

    }

}
