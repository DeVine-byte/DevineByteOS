package io.devinebyte.compiler.generator.codegen;

import io.devinebyte.compiler.blueprint.model.EntityIR;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.generator.util.TemplateEngine; // ADD THIS
import java.util.Map;

public class AggregateGenerator {
    public String generate(TenantContext tenant, EntityIR entity) {
        return TemplateEngine.render("""
            package {{package}}.domain;
            
            public record {{name}}Aggregate(String id) {
                // Rule 1: All state = fold(events)
                public {{name}}Aggregate apply(Object event) {
                    return this; // fold logic generated from EventSchema
                }
            }
            """, Map.of("package", "tenant." + tenant.tenantId(), "name", entity.name()));
    }
}
