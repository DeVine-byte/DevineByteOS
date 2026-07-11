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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CompileCommandTest {

    @Test
    void executeReturnsSuccessWhenCompilationSucceeds() {

        String[] args = {"--input", "project.db", "--output", "build"};

        CliOptions options = new CliOptions();

        Session session = mock(Session.class);
        Request request = mock(Request.class);

        Result result = mock(Result.class);
        when(result.success()).thenReturn(true);
        when(result.diagnostics()).thenReturn(List.of());

        CompilationService compilationService = mock(CompilationService.class);
        when(compilationService.compile(session, request)).thenReturn(result);

        SessionFactory sessionFactory = mock(SessionFactory.class);
        when(sessionFactory.create(options)).thenReturn(session);

        RequestMapper requestMapper = mock(RequestMapper.class);
        when(requestMapper.compileRequest(session, options)).thenReturn(request);

        OptionParser optionParser = mock(OptionParser.class);
        when(optionParser.parse(args)).thenReturn(options);

        ResultPrinter resultPrinter = mock(ResultPrinter.class);

        CompileCommand command = new CompileCommand(
                args,
                compilationService,
                sessionFactory,
                requestMapper,
                optionParser,
                resultPrinter
        );

        int exitCode = command.execute();

        assertEquals(ExitCodes.SUCCESS, exitCode);

        verify(optionParser).parse(args);
        verify(sessionFactory).create(options);
        verify(requestMapper).compileRequest(session, options);
        verify(compilationService).compile(session, request);
        verify(resultPrinter).print(result);
    }

    @Test
    void executeReturnsCompilationErrorWhenCompilationFails() {

        String[] args = {"--input", "broken.db", "--output", "build"};

        CliOptions options = new CliOptions();

        Session session = mock(Session.class);
        Request request = mock(Request.class);

        Result result = mock(Result.class);
        when(result.success()).thenReturn(false);
        when(result.diagnostics()).thenReturn(List.of("Syntax error"));

        CompilationService compilationService = mock(CompilationService.class);
        when(compilationService.compile(session, request)).thenReturn(result);

        SessionFactory sessionFactory = mock(SessionFactory.class);
        when(sessionFactory.create(options)).thenReturn(session);

        RequestMapper requestMapper = mock(RequestMapper.class);
        when(requestMapper.compileRequest(session, options)).thenReturn(request);

        OptionParser optionParser = mock(OptionParser.class);
        when(optionParser.parse(args)).thenReturn(options);

        ResultPrinter resultPrinter = mock(ResultPrinter.class);

        CompileCommand command = new CompileCommand(
                args,
                compilationService,
                sessionFactory,
                requestMapper,
                optionParser,
                resultPrinter
        );

        int exitCode = command.execute();

        assertEquals(ExitCodes.COMPILATION_ERROR, exitCode);

        verify(optionParser).parse(args);
        verify(sessionFactory).create(options);
        verify(requestMapper).compileRequest(session, options);
        verify(compilationService).compile(session, request);
        verify(resultPrinter).print(result);
    }
    }
