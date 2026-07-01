package org.devinebyte.compiler.cli;

import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilerImpl;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.cli.commands.BuildCommand;
import org.devinebyte.compiler.cli.commands.CompileCommand;
import org.devinebyte.compiler.cli.commands.HelpCommand;
import org.devinebyte.compiler.cli.commands.VersionCommand;
import org.devinebyte.compiler.core.CompilerEngine;
import org.devinebyte.compiler.core.DefaultCompilerContext;
import org.devinebyte.compiler.generator.java.JavaGenerator;
import org.devinebyte.compiler.generator.engine.SourceWriter;
import org.devinebyte.compiler.lexing.Lexer;
import org.devinebyte.compiler.parser.Parser;
import org.devinebyte.compiler.semantic.SemanticAnalyzer;
import org.devinebyte.sdk.diagnostics.DiagnosticCollector;

import java.nio.file.Path;
import java.util.Map;

public final class CliApplication {

    public static int run(String[] args) {
        if (args.length == 0) return new HelpCommand().execute();

        // Build the shared engine once - Fixes Defect 8 + 9
        var engine = new CompilerEngine(
            new Lexer(), new Parser(), new SemanticAnalyzer(),
            new BlueprintCompilerImpl(new AstToBlueprintMapper(), new BlueprintValidator()),
            new JavaGenerator(), new SourceWriter()
        );

        return switch (args[0].toLowerCase()) {
            case "compile" -> new CompileCommand(engine).execute(args);
            case "build" -> new BuildCommand(engine).execute(args);
            case "version" -> new VersionCommand().execute();
            case "help" -> new HelpCommand().execute();
            default -> {
                System.err.println("Unknown command: " + args[0]);
                yield 1;
            }
        };
    }
}
