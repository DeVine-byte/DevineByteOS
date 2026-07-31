package io.devinebyte.compiler.cli.commands;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(name = "inspect", description = "Inspect a .dbpkg file")
public class InspectCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        System.out.println("Inspecting dbpkg...");
        return 0;
    }
}
