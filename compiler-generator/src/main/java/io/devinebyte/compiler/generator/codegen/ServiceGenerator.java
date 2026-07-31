package io.devinebyte.compiler.generator.codegen;

import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.generator.util.TemplateEngine; // ADD THIS
import java.util.Map;

public class ServiceGenerator {
    public String generate(TenantContext tenant, ModuleIR module) {
        return TemplateEngine.render("""
            package {{package}}.service;
            
            public class {{name}}Service {
                // Command handlers. All writes emit Events only. No DB writes.
            }
            """, Map.of("package", "tenant." + tenant.tenantId(), "name", module.name()));
    }
}
