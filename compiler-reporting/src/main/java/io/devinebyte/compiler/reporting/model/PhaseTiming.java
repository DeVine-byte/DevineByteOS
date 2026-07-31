package io.devinebyte.compiler.reporting.model;

import java.time.Duration;

public record PhaseTiming(
    String phaseName,
    Duration duration,
    boolean success
) {}
