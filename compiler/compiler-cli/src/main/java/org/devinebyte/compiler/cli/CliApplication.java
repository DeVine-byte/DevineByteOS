package org.devinebyte.compiler.cli;

import org.devinebyte.compiler.cli.commands.CompileCommand;
import org.devinebyte.compiler.cli.console.AnsiConsole;
import org.devinebyte.compiler.cli.console.Console;
import org.devinebyte.compiler.cli.options.OptionParser;
import org.devinebyte.compiler.cli.sdk.RequestMapper;
import org.devinebyte.compiler.cli.sdk.ResultPrinter;
import org.devinebyte.compiler.cli.sdk.SessionFactory;

import org.devinebyte.sdk.internal.CompilerFacade;
import org.devinebyte.sdk.service.CompilationService;

/**
 * Bootstraps the DevineByte compiler CLI.
 *
 * <p>This class is responsible only for wiring together the CLI
 * components and delegating execution to the CompileCommand.</p>
 */
public final class CliApplication {

    private final CompilationService compilationService;
    private final SessionFactory sessionFactory;
    private final RequestMapper requestMapper;
    private final OptionParser optionParser;
    private final ResultPrinter resultPrinter;

    public CliApplication() {

        Console console = new AnsiConsole();

        this.compilationService = new CompilerFacade();
        this.sessionFactory = new SessionFactory();
        this.requestMapper = new RequestMapper();
        this.optionParser = new OptionParser();
        this.resultPrinter = new ResultPrinter(console);
    }

    /**
     * Executes the CLI.
     *
     * @param args command-line arguments
     * @return process exit code
     */
    public int run(String[] args) {

        CompileCommand command = new CompileCommand(
                args,
                compilationService,
                sessionFactory,
                requestMapper,
                optionParser,
                resultPrinter
        );

        return command.execute();
    }
}
