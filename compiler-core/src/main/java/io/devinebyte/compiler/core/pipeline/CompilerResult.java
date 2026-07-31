package io.devinebyte.compiler.core.pipeline;

import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;

public record CompilerResult<T>(
    TenantContext tenant,
    DiagnosticCollector diagnostics,
    T output
) {
    public boolean success() { return !diagnostics.hasErrors(); }
    
    public String outputPath() { 
        return output == null ? "" : output.toString(); 
    }

    public static <T> CompilerResult<T> empty(io.devinebyte.compiler.core.context.CompilationContext ctx) {
        return new CompilerResult<>(ctx.tenant(), ctx.diagnostics(), null);
    }
}
