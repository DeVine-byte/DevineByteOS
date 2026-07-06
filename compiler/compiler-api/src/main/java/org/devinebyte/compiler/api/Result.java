package org.devinebyte.compiler.api;
import java.util.List;
public record Result(boolean success, List<Diagnostic> diagnostics) {
    public static Result success() { return new Result(true, List.of()); }
    public static Result failure(List<Diagnostic> d) { return new Result(false, d); }
}
