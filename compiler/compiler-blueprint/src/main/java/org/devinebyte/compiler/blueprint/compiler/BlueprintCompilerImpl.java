package org.devinebyte.compiler.blueprint.compiler;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.model.BlueprintModel;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.blueprint.validation.ValidationResult;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.diagnostics.Severity;

import java.util.List;

/**
 * Compiles AST -> BlueprintModel -> Compiled result.
 * Uses mapper + validator instead of constructing BlueprintModel directly.
 */
public class BlueprintCompilerImpl implements BlueprintCompiler {

    private final AstToBlueprintMapper mapper;
    private final BlueprintValidator validator;

    public BlueprintCompilerImpl(AstToBlueprintMapper mapper, BlueprintValidator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public BlueprintCompilationResult compile(ProgramNode program, CompilerContext ctx) {
        // 1. Map AST to model
        BlueprintModel model = mapper.map(program);
        
        // 2. Validate and collect diagnostics
        validator.validate(model, ctx.diagnostics());
        
        // 3. Stop on ERROR, but continue on WARNING/INFO
        if (ctx.diagnostics().hasSeverity(DiagnosticSeverity.ERROR)) {
            return BlueprintCompilationResult.failed(model, ctx.diagnostics().all());
        }

        // 4. Do actual compilation
        return doCompile(model, ctx);
    }

    private BlueprintCompilationResult doCompile(BlueprintModel model, CompilerContext ctx) {
        // TODO: Replace with real compilation logic
        return BlueprintCompilationResult.success(model, ctx.diagnostics().all());
    }
}
