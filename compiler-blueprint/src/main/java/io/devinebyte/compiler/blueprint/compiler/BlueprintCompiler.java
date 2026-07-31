package io.devinebyte.compiler.blueprint.compiler;               

import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.blueprint.mapper.AuditToBlueprintMapper;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.BlueprintModel;
import io.devinebyte.compiler.blueprint.shake.ModuleTreeShaker;
import io.devinebyte.compiler.blueprint.validation.ContractViolationEngine;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.dsl.ast.AstNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class BlueprintCompiler implements CompilerPhase {

    private final ModuleCompiler moduleCompiler;
    private final ModuleTreeShaker treeShaker;
    private final ContractViolationEngine violationEngine;
    private final AuditToBlueprintMapper mapper;

    @Inject
    public BlueprintCompiler(
        ModuleCompiler moduleCompiler,
        ModuleTreeShaker treeShaker,
        ContractViolationEngine violationEngine,
        AuditToBlueprintMapper mapper
    ) {
        this.moduleCompiler = moduleCompiler;
        this.treeShaker = treeShaker;
        this.violationEngine = violationEngine;
        this.mapper = mapper;
    }

    @Override public String name() { return "blueprint"; }

    @Override
    public CompilerResult<BlueprintIR> execute(CompilationContext context, CompilerResult input) throws Exception {
        context.diagnostics().addInfo("BLUEPRINT_START", "Compiling Blueprint for tenant " + context.tenant().tenantId());

        // Cast from Object to real types
        AuditModel audit = context.get("audit");
        List<AstNode> ast = context.get("ast");

        if (audit == null || ast == null) {
            context.diagnostics().addError("BLUEPRINT_NO_INPUT", "Missing audit or ast in context");
            return new CompilerResult<>(context.tenant(), context.diagnostics(), null);
        }

        BlueprintModel model = mapper.map(context, audit);
        BlueprintIR rawIR = moduleCompiler.compile(context, audit, ast);
        CompilerResult shakenResult = treeShaker.execute(context, new CompilerResult<>(context.tenant(), context.diagnostics(), rawIR));
        BlueprintIR shakenIR = (BlueprintIR) shakenResult.output();

        // DEBUG: Compare RAW vs SHAKEN components
        System.out.println("RAW modules      : " + rawIR.modules().size());
        System.out.println("RAW entities     : " + rawIR.entities().size());
        System.out.println("RAW workflows    : " + rawIR.workflows().size());
        System.out.println("RAW events       : " + rawIR.events().size());

        System.out.println("SHAKEN modules   : " + shakenIR.modules().size());
        System.out.println("SHAKEN entities  : " + shakenIR.entities().size());
        System.out.println("SHAKEN workflows : " + shakenIR.workflows().size());
        System.out.println("SHAKEN events    : " + shakenIR.events().size());

        violationEngine.validate(context, shakenIR);

        return new CompilerResult<>(context.tenant(), context.diagnostics(), shakenIR);
    }
}

