package io.devinebyte.compiler.tests;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EntityIR;
import io.devinebyte.compiler.blueprint.model.EventIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.blueprint.model.WorkflowIR;
import io.devinebyte.compiler.blueprint.validation.ContractViolationEngine;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.core.context.TenantLifecycle;
import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;
import io.devinebyte.compiler.core.diagnostics.DiagnosticSeverity;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ContractViolationTest {

    @Test
    void workflowWithUndefinedEventFailsValidation() {
        TenantContext tenant = new TenantContext("test-tenant", TenantLifecycle.ACTIVE, Set.of("sales"));
        DiagnosticCollector diagnostics = new DiagnosticCollector();
        CompilationContext ctx = new CompilationContext(tenant, diagnostics);

        EventIR definedEvent = new EventIR("OrderCreated", "sales", "v1", Map.of("id", "String"), true);
        WorkflowIR badWorkflow = new WorkflowIR("OrderFlow", "sales", List.of("step1"), List.of("PaymentReceived"));
        
        // 7 args to match ModuleIR record
        ModuleIR module = new ModuleIR(
            "sales",                    // id
            "Sales",                    // name
            true,                       // enabled
            Set.of(),                   // dependencies
            List.of(),                  // entities
            List.of(definedEvent),      // events
            List.of(badWorkflow)        // workflows
        );

        BlueprintIR ir = new BlueprintIR(
            "test-tenant",              // tenantId
            "1.0.0",                    // version
            Set.of("sales"),            // enabledModules
            List.of(module),            // modules
            List.of(),                  // entities
            List.of(definedEvent),      // events
            List.of(badWorkflow),       // workflows
            List.of()                   // kpiFormulas
        );

        new ContractViolationEngine().validate(ctx, ir);

        assertTrue(diagnostics.hasErrors(), "Should error on undefined event. Rule 2");
        assertEquals(DiagnosticSeverity.ERROR, diagnostics.getDiagnostics().get(0).severity());
        assertEquals("CONTRACT_001", diagnostics.getDiagnostics().get(0).code());
    }
}
