package io.devinebyte.runtime.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.bootstrap.BootstrapResult;
import io.devinebyte.runtime.bootstrap.DbpkgVerifier;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector; // NEW
import io.devinebyte.runtime.module.ModuleLoader; // NEW
import io.devinebyte.runtime.module.ModuleRegistry; // NEW
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TenantIsolationE2ETest {

    @Test
    void singleDbpkgBootsTwoIsolatedTenants(@TempDir Path tmp) throws Exception {
        Path dbpkg = tmp.resolve("app.dbpkg");
        createFakeDbpkg(dbpkg);

        DiagnosticCollector diagnostics = new DiagnosticCollector();
        ModuleLoader moduleLoader = new ModuleLoader(diagnostics);
        ModuleRegistry moduleRegistry = new ModuleRegistry();

        RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(
            new DbpkgVerifier(true),
            new ManifestReader(),
            moduleLoader,
            moduleRegistry
        );
        ConfigurationManager configManager = new ConfigurationManager(new ObjectMapper());
        TenantRuntimeFactory factory = new TenantRuntimeFactory(
            configManager,
            moduleLoader,
            moduleRegistry
        );

        // Boot tenant 1 from same dbpkg
        TenantContext t1 = new TenantContext("acme-corp", TenantLifecycle.ACTIVE, Set.of());
        BootstrapResult b1 = bootstrapper.boot(t1, dbpkg);
        assertTrue(b1.success(), "Bootstrap t1 failed: " + b1.diagnostics().getAll());
        TenantRuntime r1 = factory.create(t1, b1);

        // Boot tenant 2 from SAME dbpkg
        TenantContext t2 = new TenantContext("beta-inc", TenantLifecycle.ACTIVE, Set.of());
        BootstrapResult b2 = bootstrapper.boot(t2, dbpkg);
        assertTrue(b2.success(), "Bootstrap t2 failed: " + b2.diagnostics().getAll());
        TenantRuntime r2 = factory.create(t2, b2);

        // Assertions
        assertNotSame(r1, r2, "Runtimes must be different instances");
        assertEquals("acme-corp", r1.tenantContext().tenantId());
        assertEquals("beta-inc", r2.tenantContext().tenantId());
        assertEquals(dbpkg, r1.dbpkgPath());
        assertEquals(dbpkg, r2.dbpkgPath());
    }

    private void createFakeDbpkg(Path dbpkg) throws Exception {
        String manifest = """
        {
          "schemaVersion": "1.0",
          "tenantId": "template",
          "version": "0.1.0",
          "builtAt": "%s",
          "builtBy": "test",
          "sha256": "fake",
          "signature": "fake",
          "multiTenant": true
        }
        """.formatted(Instant.now().toString());

        String moduleGraph = """
        {
          "modules": {
            "SALES": { "enabled": true, "dependsOn": [] }
          }
        }
        """;

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dbpkg.toFile()))) {
            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(manifest.getBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("runtime/module_graph.json")); // NEW: required for boot
            zos.write(moduleGraph.getBytes());
            zos.closeEntry();

            for (String dir : io.devinebyte.runtime.bootstrap.DbpkgStructure.required().requiredDirectories()) {
                zos.putNextEntry(new ZipEntry(dir + "/"));
                zos.closeEntry();
            }
        }
    }
}
