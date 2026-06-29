package org.devinebyte.compiler.testing;

import static org.assertj.core.api.Assertions.assertThat;

public final class TestAssertions {

    private TestAssertions() {
    }

    public static void compilationSucceeded(boolean success) {

        assertThat(success).isTrue();

    }

    public static void compilationFailed(boolean success) {

        assertThat(success).isFalse();

    }

}
