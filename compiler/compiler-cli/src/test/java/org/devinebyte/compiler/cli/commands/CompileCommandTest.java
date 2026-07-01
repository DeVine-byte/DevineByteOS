package org.devinebyte.compiler.cli.commands;

import org.devinebyte.compiler.cli.CompileCommand;
import org.devinebyte.compiler.core.CompilerEngine;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilerImpl;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.generator.engine.SourceWriter;
import org.devinebyte.compiler.generator.java.JavaGenerator;
import org.devinebyte.compiler.lexing.Lexer;
import org.devinebyte.compiler.parser.Parser;
import org.devinebyte.compiler.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompileCommandTest {

    @Test
    void shouldExecuteCompileCommand_Success(@TempDir Path tempDir) throws Exception {
        // 1. Build a real Engine with all stages. Fixes Defect 1
        CompilerEngine engine = new CompilerEngine(
            new Lexer(), new Parser(), new SemanticAnalyzer(),
            new BlueprintCompilerImpl(new AstToBlueprintMapper(), new BlueprintValidator()),
            new JavaGenerator(), new SourceWriter()
        );

        // 2. Create a valid .bp file so the command has something to run
        Path validFile = Files.writeString(tempDir.resolve("User.bp"), "entity User {}");

        // 3. Constructor injection now. No no-args ctor. Fixes Audit §1
        CompileCommand command = new CompileCommand(engine, new String[]{validFile.toString()});

        // 4. execute() now takes no args. Fixes Audit §1
        int exitCode = command.execute();

        assertEquals(0, exitCode); // 0 = success
    }

    @Test
    void shouldReturnNonZeroOnSemanticError(@TempDir Path tempDir) throws Exception {
        CompilerEngine engine = new CompilerEngine(
            new Lexer(), new Parser(), new SemanticAnalyzer(),
            new BlueprintCompilerImpl(new AstToBlueprintMapper(), new BlueprintValidator()),
            new JavaGenerator(), new SourceWriter()
        );

        // Duplicate entity -> should trigger SEM001 ERROR. Proves Defect 2 is fixed
        Path badFile = Files.writeString(tempDir.resolve("dup.bp"), "entity User {} entity User {}");
        CompileCommand command = new CompileCommand(engine, new String[]{badFile.toString()});

        int exitCode = command.execute();

        assertNotEquals(0, exitCode); 
    }
}
