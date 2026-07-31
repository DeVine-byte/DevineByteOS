package io.devinebyte.compiler.contracts.generator;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.contracts.model.APISchema;
import io.devinebyte.compiler.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class APISchemaGenerator {
    
    public List<APISchema> generate(TenantContext tenant, BlueprintIR ir) {
        // 1 API per Entity Command + Query
        return ir.entities().stream()
            .map(e -> new APISchema("/api/" + e.name().toLowerCase(), "POST", "Create" + e.name()))
            .toList();
    }
}
