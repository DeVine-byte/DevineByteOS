package org.devinebyte.compiler.cli.console;

public enum ConsoleColor {

    RESET("\u001B[0m"),

    RED("\u001B[31m"),

    GREEN("\u001B[32m"),

    YELLOW("\u001B[33m"),

    BLUE("\u001B[34m"),

    CYAN("\u001B[36m"),

    WHITE("\u001B[37m");

    private final String ansi;

    ConsoleColor(String ansi) {
        this.ansi = ansi;
    }

    public String ansi() {
        return ansi;
    }
}
