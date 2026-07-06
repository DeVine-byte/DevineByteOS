package org.devinebyte.compiler.testing.builders;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public final class SourceFileBuilder {

    private final StringBuilder builder =
            new StringBuilder();

    public SourceFileBuilder append(String line) {

        builder.append(line)
               .append(System.lineSeparator());

        return this;
    }

    public String build() {
        return builder.toString();
    }

}
