package org.devinebyte.compiler.cli.commands;

import org.devinebyte.compiler.cli.util.ExitCodes;
import org.devinebyte.compiler.core.CompilerEngine;
import org.devinebyte.compiler.cli.options.OptionParser;
import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.compiler.cli.console.ProgressReporter;
import org.devinebyte.compiler.cli.console.AnsiConsole;

public final class BuildCommand implements Command {

    private final String[] args;
    private final CompilerEngine engine;
    private final OptionParser parser;
    private final ProgressReporter reporter;

    public BuildCommand(String[] args, CompilerEngine engine) {
        this.args = args;
        this.engine = engine;
        this.parser = new OptionParser(); // or inject it later
        this.reporter = new ProgressReporter(new AnsiConsole());
    }

    @Override
    public int execute() {
        try {
            CliOptions options = parser.parse(args);
            
            reporter.start("Build");
            reporter.step("Resolving dependencies");
            reporter.step("Compiling");
            
            // TODO: Wire this to actually call engine.build() when you implement it
            // CompilationResult result = engine.build(options);
            
            reporter.finish("Build");
            System.out.println("Build command executed successfully");
            
            return ExitCodes.SUCCESS;
            
        } catch (Exception e) {
            System.err.println("Build failed: " + e.getMessage());
            return ExitCodes.INTERNAL_ERROR;
        }
    }
}
