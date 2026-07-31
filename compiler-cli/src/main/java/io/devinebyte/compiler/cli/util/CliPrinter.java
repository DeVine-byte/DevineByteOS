package io.devinebyte.compiler.cli.util;

import io.devinebyte.compiler.core.diagnostics.Diagnostic;

public class CliPrinter {
    public static void info(String msg) { System.out.println("[INFO] " + msg); }
    public static void success(String msg) { System.out.println("[OK] " + msg); }
    public static void error(String msg) { System.err.println("[ERROR] " + msg); }
    public static void diagnostic(Diagnostic d) { 
        System.out.println("[" + d.severity() + "] " + d.code() + ": " + d.message()); 
    }
}
