package compiler.core.test;

import compiler.core.bootstrap.Compiler;
import compiler.core.context.CompilationContext;
import compiler.core.diagnostics.DiagnosticCollector;
import compiler.core.pipeline.CompilerPipeline;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerTest {

    @Test
    public void testCompilerSuccessFlow() {

        DiagnosticCollector diagnostics = new DiagnosticCollector();

        CompilerPipeline pipeline = new CompilerPipeline(List.of());

        Compiler compiler = new Compiler(pipeline, diagnostics);

        CompilationContext context =
                new CompilationContext("tenant-1", "test-source", Map.of());

        var result = compiler.compile(context);

        assertNotNull(result);
    }
}
