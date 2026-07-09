package org.devinebyte.compiler.cli.commands;

import org.devinebyte.sdk.Session;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.service.CompilationService;
import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.compiler.cli.options.OptionParser;
import org.devinebyte.compiler.cli.sdk.RequestMapper;
import org.devinebyte.compiler.cli.sdk.SessionFactory;
import org.devinebyte.compiler.cli.sdk.ResultPrinter;
import org.devinebyte.compiler.cli.util.ExitCodes;

public final class CompileCommand implements Command {

    private final String[] args;
    private final CompilationService compilationService;
    private final SessionFactory sessionFactory;
    private final RequestMapper mapper;
    private final OptionParser parser;
    private final ResultPrinter resultPrinter;

    public CompileCommand(
            String[] args,
            CompilationService compilationService,
            SessionFactory sessionFactory,
            RequestMapper mapper,
            OptionParser parser,
            ResultPrinter resultPrinter) {
        this.args = args;
        this.compilationService = compilationService;
        this.sessionFactory = sessionFactory;
        this.mapper = mapper;
        this.parser = parser;
        this.resultPrinter = resultPrinter;
    }

    @Override
    public int execute() {
        CliOptions options = parser.parse(args);
        
        // New SDK flow
        Session session = sessionFactory.create(options);
        Request request = mapper.compileRequest(session, options);
        Result result = compilationService.compile(session, request);
        
        resultPrinter.print(result);
        return result.success() ? ExitCodes.SUCCESS : ExitCodes.COMPILATION_ERROR;
    }
}
