package io.devinebyte.runtime.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.devinebyte.compiler.packaging.model.Manifest;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeBootstrapperTest {

    private final RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(
        new DbpkgVerifier(),
        new ManifestReader(),
        new ModuleLoader(new DiagnosticCollector()),
        new ModuleRegistry(),
        new WorkflowEngine(null, null, null)
    );

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Path dbpkgPath;
    private final TenantContext acme = new TenantContext("acme", TenantLifecycle.ACTIVE, Set.of("SALES"));

    // Define a matching JSON mapping record for test manifest generation
    private record TestManifest(
        String schemaVersion, 
        String tenantId, 
        String version, 
        Instant builtAt, 
        String builtBy, 
        @JsonProperty("sha256") String checksumSha256, 
        String signature, 
        boolean multiTenant
    ) {}

    @BeforeEach
    void setup(@TempDir Path tempDir) throws Exception {
        dbpkgPath = tempDir.resolve("test-bootstrap.dbpkg");
        
        String moduleGraph = """
        {
          "modules": {
            "runtime": {
              "moduleId": "runtime",
              "enabled": true,
              "dependsOn": [],
              "exposesEvents": ["SystemBooted"],
              "subscribesToEvents": []
            }
          }
        }
        """;
        String apiSchema = "[]";

        // Compute secure content hash matching alphabetical sort: contracts/ before runtime/
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(apiSchema.getBytes(StandardCharsets.UTF_8));
        digest.update(moduleGraph.getBytes(StandardCharsets.UTF_8));
        String trueContentSha = HexFormat.of().formatHex(digest.digest());

        TestManifest testManifest = new TestManifest(
            "2.0", "acme", "1.0.0", Instant.now(), "test-compiler", trueContentSha, "fake-sig", true
        );

        // Build a perfectly aligned test archive
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dbpkgPath.toFile()))) {
            writeEntry(zos, "contracts/APISchema.json", apiSchema);
            writeEntry(zos, "runtime/module_graph.json", moduleGraph);
            writeDir(zos, "contracts/");
            writeDir(zos, "workflows/");
            writeDir(zos, "projections/");
            writeDir(zos, "runtime/");
            writeDir(zos, "bootstrap/");
            writeEntry(zos, "manifest.json", MAPPER.writeValueAsString(testManifest));
        }
    }

    @Test
    void boot_succeedsForCorrectTenant() {
        BootstrapResult result = bootstrapper.boot(acme, dbpkgPath);
        assertTrue(result.success(), "Boot integration sequence failed under matching parameters context.");
        assertEquals("acme", result.manifest().tenantId());
    }

    @Test
    void boot_respectsMultiTenantFlag() {
        TenantContext wrong = new TenantContext("wrong-tenant", TenantLifecycle.ACTIVE, Set.of("SALES"));
        BootstrapResult result = bootstrapper.boot(wrong, dbpkgPath);

        // Since our test archive sets multiTenant = true, it should accept the tenant routing block smoothly
        assertTrue(result.success(), "Template shared scope packages must permit matching secondary tenant entities.");
    }

    private void writeEntry(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void writeDir(ZipOutputStream zos, String name) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.closeEntry();
    }
}

