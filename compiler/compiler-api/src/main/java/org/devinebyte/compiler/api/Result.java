package org.devinebyte.compiler.api;

import java.util.List;

public record Result(boolean success, List<Diagnostic> diagnostics) {

    public static Result ok() {
        return new Result(true, List.of());
    }

    public static Result fail(List<Diagnostic> diagnostics) {
        return new Result(false, diagnostics);
    }
}
