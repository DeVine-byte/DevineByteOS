package org.devinebyte.compiler.testing.assertions;

import org.devinebyte.sdk.diagnostics.Diagnostic;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertions for compiler diagnostics.
 */
public final class DiagnosticAssertions {

    private DiagnosticAssertions() {
    }

    /**
     * Asserts that no diagnostics were produced.
     */
    public static void assertEmpty(Iterable<Diagnostic> diagnostics) {
        assertThat(diagnostics)
                .isNotNull()
                .isEmpty();
    }

    /**
     * Asserts that diagnostics were produced.
     */
    public static void assertNotEmpty(Iterable<Diagnostic> diagnostics) {
        assertThat(diagnostics)
                .isNotNull()
                .isNotEmpty();
    }

    /**
     * Asserts the expected number of diagnostics.
     */
    public static void assertCount(
            Iterable<Diagnostic> diagnostics,
            int expected) {

        assertThat(diagnostics)
                .hasSize(expected);
    }

    /**
     * Asserts that at least one diagnostic contains the given message.
     */
    public static void assertContainsMessage(
            Iterable<Diagnostic> diagnostics,
            String message) {

        assertThat(diagnostics)
                .extracting(Diagnostic::getMessage)
                .anyMatch(text -> text.contains(message));
    }

    /**
     * Asserts that diagnostics contain the expected severity.
     */
    public static void assertContainsSeverity(
            Iterable<Diagnostic> diagnostics,
            DiagnosticSeverity severity) {

        assertThat(diagnostics)
                .extracting(Diagnostic::getSeverity)
                .contains(severity);
    }

    /**
     * Asserts that at least one error diagnostic exists.
     */
    public static void assertHasErrors(
            Iterable<Diagnostic> diagnostics) {

        assertThat(diagnostics)
                .anyMatch(Diagnostic::isError);
    }

    /**
     * Asserts that no error diagnostics exist.
     */
    public static void assertHasNoErrors(
            Iterable<Diagnostic> diagnostics) {

        assertThat(diagnostics)
                .noneMatch(Diagnostic::isError);
    }
}
