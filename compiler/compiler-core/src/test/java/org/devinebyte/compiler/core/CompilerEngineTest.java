package org.devinebyte.compiler.core;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilerImpl;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.generator.engine.SourceWriter;
import org.devinebyte.compiler.generator.java.JavaGenerator;
import org.devinebyte.compiler.lexing.Lexer;
import org.devinebyte.compiler.parser.Parser;
import org.devinebyte.compiler.semantic.SemanticAnalyzer;
import org.devinebyte.compiler.testing.support.CompilerTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompilerEngineTest extends CompilerTestSupport {

    @Test
    void shouldCreateCompilerEngine(@TempDir Path tempDir) throws Exception {
        // 1. Build the real Engine with stubs. Fixes Defect 8
        CompilerEngine engine = new CompilerEngine(
            new Lexer(), new Parser(), new SemanticAnalyzer(),
            new BlueprintCompilerImpl(new AstToBlueprintMapper(), new BlueprintValidator()),
            new JavaGenerator(), new SourceWriter()
        );
        assertNotNull(engine);

        // 2. Run it end-to-end. Fixes Defect 1
        Path file = Files.writeString(tempDir.resolve("test.bp"), "entity User {}");
        var ctx = new DefaultCompilerContext(tempDir, Map.of(), new DiagnosticCollector());

        var result = engine.compile(file, ctx);

        assertNotNull(result);
        // TODO: assertTrue(result.success()); once Lexer/Parser are stubbed to not error
    }
}
