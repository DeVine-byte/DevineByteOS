package org.devinebyte.compiler.cli.console;

public class AnsiConsole implements Console {

    private final boolean colorsEnabled;

    public AnsiConsole() {
        this.colorsEnabled = System.console() != null;
    }

    @Override
    public void info(String message) {
        printColored(message, ConsoleColor.BLUE);
    }

    @Override
    public void success(String message) {
        printColored(message, ConsoleColor.GREEN);
    }

    @Override
    public void warning(String message) {
        printColored(message, ConsoleColor.YELLOW);
    }

    @Override
    public void error(String message) {
        printColored(message, ConsoleColor.RED);
    }

    @Override
    public void print(String message) {
        System.out.println(message);
    }

    private void printColored(String message, ConsoleColor color) {

        if (colorsEnabled) {
            System.out.println(color.ansi() + message + ConsoleColor.RESET.ansi());
        } else {
            System.out.println(message);
        }
    }
}
