package io.devinebyte.compiler.cli;

import io.devinebyte.compiler.cli.commands.CompileCommand;
import io.devinebyte.compiler.cli.commands.PackageCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "devinebyte", 
    subcommands = {CompileCommand.class, PackageCommand.class},
    description = "DevineByteOS Compiler")
public class CompilerCli {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CompilerCli()).execute(args);
        System.exit(exitCode);
    }
}
