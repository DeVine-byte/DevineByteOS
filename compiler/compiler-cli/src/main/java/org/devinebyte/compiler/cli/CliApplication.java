package org.devinebyte.compiler.cli;

import org.devinebyte.compiler.cli.commands.CompileCommand;
import org.devinebyte.compiler.cli.console.AnsiConsole;
import org.devinebyte.compiler.cli.console.Console;
import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.compiler.cli.options.OptionParser;
import org.devinebyte.compiler.cli.sdk.RequestMapper;
import org.devinebyte.compiler.cli.sdk.ResultPrinter;
import org.devinebyte.compiler.cli.sdk.SessionFactory;
import org.devinebyte.sdk.CompilerSDK;

public final class CliApplication {

    private final OptionParser optionParser;
    private final SessionFactory sessionFactory;
    private final RequestMapper requestMapper;
    private final ResultPrinter resultPrinter;

    public CliApplication() {

        Console console = new AnsiConsole();

        this.optionParser = new OptionParser();
        this.sessionFactory = new SessionFactory(new CompilerSDK());
        this.requestMapper = new RequestMapper();
        this.resultPrinter = new ResultPrinter(console);
    }

    public int run(String[] args) {

        CliOptions options = optionParser.parse(args);

        CompileCommand command = new CompileCommand(
                options,
                sessionFactory,
                requestMapper,
                resultPrinter
        );

        return command.execute();
    }
}
