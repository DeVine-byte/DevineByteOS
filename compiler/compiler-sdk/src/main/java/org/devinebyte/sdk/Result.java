package org.devinebyte.sdk;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Result {

    private final boolean success;
    private final List<Path> artifacts;
    private final List<String> diagnostics;

    public Result(
            boolean success,
            List<Path> artifacts,
            List<String> diagnostics) {

        this.success = success;
        this.artifacts = List.copyOf(
                Objects.requireNonNullElse(artifacts, Collections.emptyList()));
        this.diagnostics = List.copyOf(
                Objects.requireNonNullElse(diagnostics, Collections.emptyList()));
    }

    public boolean success() {
        return success;
    }

    public List<Path> artifacts() {
        return artifacts;
    }

    public List<String> diagnostics() {
        return diagnostics;
    }

    public boolean hasDiagnostics() {
        return !diagnostics.isEmpty();
    }

    public static Result successful(List<Path> artifacts) {
        return new Result(true, artifacts, Collections.emptyList());
    }

    public static Result failed(List<String> diagnostics) {
        return new Result(false, Collections.emptyList(), diagnostics);
    }
}
