package io.devinebyte.runtime.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TenantIsolationE2ETest {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void singleDbpkgBootsTwoIsolatedTenants(@TempDir Path tempDir) throws Exception {
        Path dbpkg = tempDir.resolve("test.dbpkg");
        createValidDbpkg(dbpkg);
        Path baseData = Path.of("build/data/tenants");
        
        if (Files.exists(baseData)) {
            Files.walk(baseData)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(java.io.File::delete);
        }

        RuntimeLauncher.launch(dbpkg, "tenant1");
        RuntimeLauncher.launch(dbpkg, "tenant2");
        Thread.sleep(150); 

        assertTrue(Files.exists(baseData.resolve("tenant1/events.log")), "Tenant 1 events registry file should exist.");
        assertTrue(Files.exists(baseData.resolve("tenant2/events.log")), "Tenant 2 events registry file should exist.");
        assertNotEquals(
            Files.readString(baseData.resolve("tenant1/events.log")), 
            Files.readString(baseData.resolve("tenant2/events.log")),
            "Tenants data tracking states must remain entirely isolated."
        );
    }

    private void createValidDbpkg(Path dbpkg) throws Exception {
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

        // FIXED: Order payload parts alphabetically to mirror the verifier's sorted entry validation mechanism
        // "contracts/APISchema.json" comes alphabetically BEFORE "runtime/module_graph.json"
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(apiSchema.getBytes(StandardCharsets.UTF_8));
        digest.update(moduleGraph.getBytes(StandardCharsets.UTF_8));
        String trueContentSha = HexFormat.of().formatHex(digest.digest());

        record TestManifest(
            String schemaVersion, 
            String tenantId, 
            String version, 
            Instant builtAt, 
            String builtBy, 
            @JsonProperty("sha256") String checksumSha256, 
            String signature, 
            boolean multiTenant
        ) {}

        TestManifest verifiedManifest = new TestManifest("2.0", "template", "0.1.0", Instant.now(), "test-suite", trueContentSha, "secure-signature", true);
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dbpkg.toFile()))) {
            writeEntry(zos, "contracts/APISchema.json", apiSchema);
            writeEntry(zos, "runtime/module_graph.json", moduleGraph);
            writeDir(zos, "contracts/");
            writeDir(zos, "workflows/");
            writeDir(zos, "projections/");
            writeDir(zos, "runtime/");
            writeDir(zos, "bootstrap/");
            writeEntry(zos, "manifest.json", MAPPER.writeValueAsString(verifiedManifest));
        }
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

