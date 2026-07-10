package org.devinebyte.compiler.testing.assertions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * General-purpose assertions for compiler tests.
 */
public final class TestAssertions {

    private TestAssertions() {
        // Utility class
    }

    /**
     * Asserts that a condition is true.
     *
     * @param condition condition to verify
     */
    public static void assertTrue(boolean condition) {
        assertThat(condition).isTrue();
    }

    /**
     * Asserts that a condition is false.
     *
     * @param condition condition to verify
     */
    public static void assertFalse(boolean condition) {
        assertThat(condition).isFalse();
    }

    /**
     * Asserts that an object is not null.
     *
     * @param value object to verify
     */
    public static void assertNotNull(Object value) {
        assertThat(value).isNotNull();
    }

    /**
     * Asserts that an object is null.
     *
     * @param value object to verify
     */
    public static void assertNull(Object value) {
        assertThat(value).isNull();
    }

    /**
     * Asserts that two values are equal.
     *
     * @param expected expected value
     * @param actual actual value
     */
    public static void assertEquals(Object expected, Object actual) {
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * Asserts that two values are different.
     *
     * @param unexpected unexpected value
     * @param actual actual value
     */
    public static void assertNotEquals(Object unexpected, Object actual) {
        assertThat(actual).isNotEqualTo(unexpected);
    }
}
