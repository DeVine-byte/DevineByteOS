package io.devinebyte.compiler.audit.phase;

import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.audit.parser.AuditParseResult;
import io.devinebyte.compiler.audit.parser.AuditParser;
import io.devinebyte.compiler.audit.validation.AuditValidationEngine;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton 
public class AuditPhase implements CompilerPhase {
    private final AuditParser parser; 
    private final AuditValidationEngine validator;

    @Inject 
    public AuditPhase(AuditParser parser, AuditValidationEngine validator) {
        this.parser = parser; 
        this.validator = validator;
    }

    @Override public String name() { return "audit"; }
    
    @Override
    public CompilerResult<AuditModel> execute(CompilationContext ctx, CompilerResult input) throws Exception {
        Path auditPath = Path.of("samples/" + ctx.tenant().tenantId() + ".dbdsl");
        String source = Files.readString(auditPath);
        
        AuditParseResult parseResult = parser.parse(ctx, source); // <-- 2 args
        AuditModel audit = parseResult.model();
        
        validator.validate(ctx, audit);
        ctx.put("audit", audit); 
        return new CompilerResult<>(ctx.tenant(), ctx.diagnostics(), audit);
    }
}
