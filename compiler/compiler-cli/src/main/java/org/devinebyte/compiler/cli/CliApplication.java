package org.devinebyte.compiler.cli;

import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilerImpl;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator; // fixed package
import org.devinebyte.compiler.cli.commands.*;
import org.devinebyte.compiler.cli.options.OptionParser;
import org.devinebyte.compiler.cli.sdk.*;
import org.devinebyte.compiler.cli.util.ExitCodes;
import org.devinebyte.sdk.service.CompilationService;
import org.devinebyte.sdk.service.CompilationServiceImpl; // assuming you have this
import org.devinebyte.compiler.core.CompilerEngine;
import org.devinebyte.compiler.generator.java.JavaGenerator;
import org.devinebyte.compiler.generator.engine.SourceWriter;
import org.devinebyte.compiler.lexing.Lexer;
import org.devinebyte.compiler.parser.Parser;
import org.devinebyte.compiler.semantic.SemanticAnalyzer;

public final class CliApplication {

    public static int run(String[] args) {
        if (args.length == 0) return new HelpCommand().execute();

        // 1. Build shared engine/pipeline once
        var engine = new CompilerEngine(
            new Lexer(), new Parser(), new SemanticAnalyzer(),
            new BlueprintCompilerImpl(new AstToBlueprintMapper(), new BlueprintValidator()),
            new JavaGenerator(), new SourceWriter()
        );

        // 2. Build CLI services from engine
        CompilationService compilationService = new CompilationServiceImpl(engine);
        SessionFactory sessionFactory = new SessionFactory();
        RequestMapper mapper = new RequestMapper();
        OptionParser parser = new OptionParser();
        ResultPrinter printer = new ResultPrinter();

        return switch (args[0].toLowerCase()) {
            case "compile" -> new CompileCommand(
                    args, compilationService, sessionFactory, mapper, parser, printer
            ).execute();

            case "build" -> new BuildCommand(args, engine).execute(); // we’ll fix this next
            case "version" -> new VersionCommand().execute();
            case "help" -> new HelpCommand().execute();
            default -> {
                System.err.println("Unknown command: " + args[0]);
                yield ExitCodes.INVALID_ARGUMENT;
            }
        };
    }
}
