package org.devinebyte.sdk;

/**
 * Entry point for the DevineByte Compiler SDK.
 *
 * <p>The SDK exposes a fluent builder for creating compiler
 * sessions. A session encapsulates compiler configuration
 * and can execute one or more compilation requests.</p>
 */
public final class CompilerSDK {

    private CompilerSDK() {
        // Utility class
    }

    /**
     * Creates a new SDK builder.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
