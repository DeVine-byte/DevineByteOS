package org.devinebyte.compiler.testing.assertions;

import static org.assertj.core.api.Assertions.assertThat;

public final class CompilationAssertions {

    private CompilationAssertions() {
    }

    public static void succeeded(boolean success) {

        assertThat(success).isTrue();

    }

}
