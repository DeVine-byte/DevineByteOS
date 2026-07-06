package org.devinebyte.compiler.cli.commands;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.compiler.cli.options.OptionParser;
import org.devinebyte.compiler.cli.sdk.RequestMapper;
import org.devinebyte.compiler.cli.sdk.SessionFactory;
import org.devinebyte.compiler.cli.sdk.ResultPrinter;
import org.devinebyte.compiler.cli.util.ExitCodes;
import org.devinebyte.compiler.api.service.CompilationService;
import org.devinebyte.compiler.api.session.BuildSession;
import org.devinebyte.compiler.api.request.CompilerRequest;
import org.devinebyte.compiler.api.result.CompilerResult;

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
        BuildSession session = sessionFactory.create();
        CompilerRequest request = mapper.compileRequest(options);
        CompilerResult result = compilationService.compile(session, request);
        resultPrinter.print(result);
        return result.success()? ExitCodes.SUCCESS : ExitCodes.COMPILATION_ERROR;
    }
}
