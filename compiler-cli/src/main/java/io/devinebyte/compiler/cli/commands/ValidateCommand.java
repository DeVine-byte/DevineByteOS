package io.devinebyte.compiler.cli.commands;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(name = "validate", description = "Validate contracts and module isolation")
public class ValidateCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        System.out.println("Validating contracts...");
        return 0;
    }
}
