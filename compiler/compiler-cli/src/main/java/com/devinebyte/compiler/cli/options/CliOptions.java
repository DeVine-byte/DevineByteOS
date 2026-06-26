package com.devinebyte.compiler.cli.options;

import java.nio.file.Path;

public class CliOptions {

    private Path input;
    private Path output;

    private boolean incremental;
    private boolean optimize;
    private boolean strict;
    private boolean verbose;

    public Path getInput() {
        return input;
    }

    public void setInput(Path input) {
        this.input = input;
    }

    public Path getOutput() {
        return output;
    }

    public void setOutput(Path output) {
        this.output = output;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
