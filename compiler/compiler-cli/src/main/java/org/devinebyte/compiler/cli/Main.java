package org.devinebyte.compiler.cli;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        CliApplication application = new CliApplication();
        int exitCode = application.run(args);

        System.exit(exitCode);
    }
}
