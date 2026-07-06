package org.devinebyte.compiler.cli.util;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public final class ExitCodes {

    public static final int SUCCESS = 0;

    public static final int COMPILATION_ERROR = 1;

    public static final int VALIDATION_ERROR = 2;

    public static final int INVALID_ARGUMENT = 3;

    public static final int IO_ERROR = 4;

    public static final int CONFIGURATION_ERROR = 5;

    public static final int INTERNAL_ERROR = 10;

    private ExitCodes() {
        throw new AssertionError("Utility class");
    }

}
