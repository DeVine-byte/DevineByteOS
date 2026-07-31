package io.devinebyte.compiler.blueprint.mapper;

import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.blueprint.model.BlueprintModel;
import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Singleton;

@Singleton
public class AuditToBlueprintMapper {
    public BlueprintModel map(CompilationContext context, AuditModel audit) {
        return new BlueprintModel(
            context.tenant().tenantId(),
            audit.companyName(),
            audit.targetLifecycle(),
            context.tenant().enabledModules(),
            audit.businessUnits(),
            audit.processes(),
            audit.kpis(),
            audit.recommendations()
        );
    }
}
