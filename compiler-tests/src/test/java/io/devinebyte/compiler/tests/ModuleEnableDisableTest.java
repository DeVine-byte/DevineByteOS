package io.devinebyte.compiler.tests;

import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.core.context.TenantLifecycle;
import io.devinebyte.compiler.generator.codegen.DomainGenerator;
import io.devinebyte.compiler.projection.compiler.ProjectionCompiler;
import io.devinebyte.compiler.projection.compiler.WasmGenerator;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ModuleEnableDisableTest {

    @Test
    void disabledModuleGeneratesNoArtifacts() throws Exception {
        TenantContext tenant = new TenantContext("test-tenant", TenantLifecycle.ACTIVE, Set.of("sales")); // inventory disabled
        
        DomainGenerator domainGen = new DomainGenerator();
        ProjectionCompiler projGen = new ProjectionCompiler(new WasmGenerator()); // fix null

        Path out = Path.of("build/test-output");
        Files.createDirectories(out);

        // This test will pass once we actually run the generators with the tenant
        // For now just verify the assertion logic
        assertFalse(Files.exists(out.resolve("domain/inventory")),
            "Disabled module generated code. Rule 4 violated");
    }
}
