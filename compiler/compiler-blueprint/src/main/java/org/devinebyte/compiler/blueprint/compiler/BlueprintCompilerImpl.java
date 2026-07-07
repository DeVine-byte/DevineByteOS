package org.devinebyte.compiler.blueprint.compiler;

import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.model.BlueprintModel;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.blueprint.validation.ValidationResult;
import org.devinebyte.compiler.parser.ast.ProgramNode;

public class BlueprintCompilerImpl implements BlueprintCompiler {

    private final AstToBlueprintMapper mapper;
    private final BlueprintValidator validator;

    public BlueprintCompilerImpl(
            AstToBlueprintMapper mapper,
            BlueprintValidator validator) {

        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public BlueprintCompilationResult compile(
            ProgramNode program,
            CompilerContext ctx) {

        BlueprintModel model = mapper.map(program);

        ValidationResult validation = validator.validate(model);

        for (String error : validation.getErrors()) {
            ctx.diagnostics().add(
                    new Diagnostic(
                            DiagnosticSeverity.ERROR,
                            "BP001",
                            error,
                            0,
                            0
                    )
            );
        }

        if (!validation.isValid()) {
            return BlueprintCompilationResult.failed(
                    model,
                    ctx.diagnostics()
            );
        }

        return BlueprintCompilationResult.success(
                model,
                ctx.diagnostics()
        );
    }
}
