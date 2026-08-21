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
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter; // CHANGED
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class BlueprintCompiler implements CompilerPhase {

    private final ModuleCompiler moduleCompiler;
    private final ModuleTreeShaker treeShaker;
    private final ContractViolationEngine violationEngine;
    private final AuditToBlueprintMapper mapper;
    private final ApiSchemaWriter apiSchemaWriter; // FIXED: added ;

    @Inject
    public BlueprintCompiler(
        ModuleCompiler moduleCompiler,
        ModuleTreeShaker treeShaker,
        ContractViolationEngine violationEngine,
        AuditToBlueprintMapper mapper,
        ApiSchemaWriter apiSchemaWriter
    ) {
        this.moduleCompiler = moduleCompiler;
        this.treeShaker = treeShaker;
        this.violationEngine = violationEngine;
        this.mapper = mapper;
        this.apiSchemaWriter = apiSchemaWriter;
    }

    @Override public String name() { return "blueprint"; }

    @Override
    public CompilerResult<BlueprintIR> execute(CompilationContext context, CompilerResult input) throws Exception {
        context.diagnostics().addInfo("BLUEPRINT_START", "Compiling Blueprint for tenant " + context.tenant().tenantId());

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

    // DELETE THESE 2 LINES
    // List<ApiSchemaWriter.ApiSchema> apiSchemas = apiSchemaWriter.generate(context, ast);
    // context.put("apiSchemas", apiSchemas);
    // context.diagnostics().addInfo("API_GEN", "Generated " + apiSchemas.size() + " API contracts");

        System.out.println("RAW modules      : " + rawIR.modules().size());
    // ... rest

        violationEngine.validate(context, shakenIR);

    // Rebuild IR WITHOUT apiSchemas
        BlueprintIR finalIR = new BlueprintIR(
            shakenIR.tenantId(), shakenIR.version(), shakenIR.enabledModules(),
            shakenIR.modules(), shakenIR.entities(), shakenIR.events(),
            shakenIR.workflows(), shakenIR.kpiFormulas(), List.of() // EMPTY LIST
        );

        return new CompilerResult<>(context.tenant(), context.diagnostics(), finalIR);
    }
}
