package org.devinebyte.compiler.cli.sdk;

import org.devinebyte.compiler.cli.console.Console;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ResultPrinterTest {

    @Test
    void printsSuccessfulCompilation() {

        Console console = mock(Console.class);

        Result result = mock(Result.class);
        when(result.success()).thenReturn(true);
        when(result.diagnostics()).thenReturn(List.of());

        ResultPrinter printer = new ResultPrinter(console);

        printer.print(result);

        verify(console).success("Compilation completed successfully.");
        verify(console, never()).error(anyString());
    }

    @Test
    void printsFailedCompilation() {

        Console console = mock(Console.class);

        Result result = mock(Result.class);
        when(result.success()).thenReturn(false);
        when(result.diagnostics()).thenReturn(List.of());

        ResultPrinter printer = new ResultPrinter(console);

        printer.print(result);

        verify(console).error("Compilation failed.");
        verify(console, never()).success(anyString());
    }

    @Test
    void printsDiagnostics() {

        Console console = mock(Console.class);

        Result result = mock(Result.class);
        when(result.success()).thenReturn(false);
        when(result.diagnostics()).thenReturn(List.of(
                "Error 1",
                "Error 2"
        ));

        ResultPrinter printer = new ResultPrinter(console);

        printer.print(result);

        verify(console).error("Compilation failed.");
        verify(console).print("");
        verify(console).print("Error 1");
        verify(console).print("Error 2");
    }

    @Test
    void doesNotPrintBlankLineWhenNoDiagnostics() {

        Console console = mock(Console.class);

        Result result = mock(Result.class);
        when(result.success()).thenReturn(true);
        when(result.diagnostics()).thenReturn(List.of());

        ResultPrinter printer = new ResultPrinter(console);

        printer.print(result);

        verify(console, never()).print("");
    }
}
