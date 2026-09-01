package io.devinebyte.runtime.bootstrap;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine; // FIX: Added import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeBootstrapperTest {

    // FIX: Pass all 5 dependencies now by adding a dummy WorkflowEngine instance
    private final RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(
        new DbpkgVerifier(true),
        new ManifestReader(),
        new ModuleLoader(new DiagnosticCollector()),
        new ModuleRegistry(),
        new WorkflowEngine(null, null) 
    );

    private Path dbpkgPath;
    private ManifestReader.Manifest manifest; // pre-read it
    private final TenantContext acme = new TenantContext("acme", TenantLifecycle.ACTIVE, Set.of("SALES"));

    @BeforeEach
    void setup() throws Exception {
        String prop = System.getProperty("dbpkg.path");
        dbpkgPath = prop != null ? Path.of(prop) : Path.of("execution/acme/tenant-acme-v1.0.0.dbpkg");
        assertTrue(Files.exists(dbpkgPath), "DBPKG not found: " + dbpkgPath);

        // Pre-read manifest so we know if it's template or strict BEFORE booting
        try (ZipFile zip = new ZipFile(dbpkgPath.toFile())) {
            var entry = zip.getEntry("manifest.json");
            manifest = new ManifestReader().read(acme, zip.getInputStream(entry), new DiagnosticCollector());
        }
        assertNotNull(manifest);
        System.out.println("MANIFEST: tenant=" + manifest.tenantId() + " multiTenant=" + manifest.multiTenant());
    }

    @Test
    void boot_succeedsForCorrectTenant() {
        BootstrapResult result = bootstrapper.boot(acme, dbpkgPath);
        assertTrue(result.success(), "Should boot for correct tenant");
        assertEquals("acme", result.manifest().tenantId());
    }

    @Test
    void boot_respectsMultiTenantFlag() {
        TenantContext wrong = new TenantContext("wrong-tenant", TenantLifecycle.ACTIVE, Set.of("SALES"));
        BootstrapResult result = bootstrapper.boot(wrong, dbpkgPath);

        if (manifest.multiTenant()) {
            // TEMPLATE mode
            assertTrue(result.success(), "Template dbpkg should allow any tenant. Diagnostics: " + result.diagnostics().getAll());
            System.out.println("TEMPLATE: boot succeeded for wrong-tenant as expected");
        } else {
            // STRICT mode
            assertFalse(result.success(), "Strict dbpkg should block wrong tenant");
            assertTrue(result.diagnostics().getAll().stream().anyMatch(d -> "DBRT007".equals(d.code())),
                "Expected DBRT007 Tenant Mismatch error");
            System.out.println("STRICT: boot failed with DBRT007 as expected");
        }
    }
}

