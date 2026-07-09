package org.devinebyte.compiler.cli.commands;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.compiler.cli.options.OptionParser;
import org.devinebyte.compiler.cli.sdk.RequestMapper;
import org.devinebyte.compiler.cli.sdk.ResultPrinter;
import org.devinebyte.compiler.cli.sdk.SessionFactory;
import org.devinebyte.compiler.cli.util.ExitCodes;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;
import org.devinebyte.sdk.service.CompilationService;

public final class CompileCommand implements Command {

    private final String[] args;
    private final CompilationService compilationService;
    private final SessionFactory sessionFactory;
    private final RequestMapper requestMapper;
    private final OptionParser optionParser;
    private final ResultPrinter resultPrinter;

    public CompileCommand(
            String[] args,
            CompilationService compilationService,
            SessionFactory sessionFactory,
            RequestMapper requestMapper,
            OptionParser optionParser,
            ResultPrinter resultPrinter) {

        this.args = args;
        this.compilationService = compilationService;
        this.sessionFactory = sessionFactory;
        this.requestMapper = requestMapper;
        this.optionParser = optionParser;
        this.resultPrinter = resultPrinter;
    }

    @Override
    public int execute() {

        CliOptions options = optionParser.parse(args);

        Session session = sessionFactory.create(options);

        Request request = requestMapper.compileRequest(session, options);

        Result result = compilationService.compile(session, request);

        resultPrinter.print(result);

        return result.success()
                ? ExitCodes.SUCCESS
                : ExitCodes.COMPILATION_ERROR;
    }
}
