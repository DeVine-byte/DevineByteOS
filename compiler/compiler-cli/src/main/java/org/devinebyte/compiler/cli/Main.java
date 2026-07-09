package org.devinebyte.compiler.cli;

/**
 * Entry point for the DevineByte Compiler CLI.
 */
public final class Main {

    private Main() {
        // Prevent instantiation
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        CliApplication application = new CliApplication();

        int exitCode = application.run(args);

        System.exit(exitCode);
    }
}
