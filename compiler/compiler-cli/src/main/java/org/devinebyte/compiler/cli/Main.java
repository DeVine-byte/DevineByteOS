package org.devinebyte.compiler.cli;

public final class Main {

    private Main() {
        throw new AssertionError("Utility class");
    }

    public static void main(String[] args) {
        int exitCode = CliApplication.run(args);
        System.exit(exitCode);
    }

}
