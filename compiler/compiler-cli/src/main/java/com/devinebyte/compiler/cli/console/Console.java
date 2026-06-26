package com.devinebyte.compiler.cli.console;

public interface Console {

    void info(String message);

    void success(String message);

    void warning(String message);

    void error(String message);

    void print(String message);
}
