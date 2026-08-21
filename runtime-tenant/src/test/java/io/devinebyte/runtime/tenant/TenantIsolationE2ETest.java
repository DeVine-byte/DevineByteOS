package io.devinebyte.runtime.tenant;

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
    // FIX: disable WRITE_DATES_AS_TIMESTAMPS so Instant becomes ISO string
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void singleDbpkgBootsTwoIsolatedTenants(@TempDir Path tempDir) throws Exception {
        Path dbpkg = tempDir.resolve("test.dbpkg");
        createValidDbpkg(dbpkg);
        Path baseData = Path.of("build/data/tenants");
        if (Files.exists(baseData)) Files.walk(baseData).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);

        RuntimeLauncher.launch(dbpkg, "tenant1", true);
        RuntimeLauncher.launch(dbpkg, "tenant2", true);
        Thread.sleep(100); // let FileEventStore flush

        assertTrue(Files.exists(baseData.resolve("tenant1/events.log")));
        assertTrue(Files.exists(baseData.resolve("tenant2/events.log")));
        assertNotEquals(Files.readString(baseData.resolve("tenant1/events.log")), Files.readString(baseData.resolve("tenant2/events.log")));
    }

    private void createValidDbpkg(Path dbpkg) throws Exception {
        record TestManifest(String schemaVersion, String tenantId, String version, Instant builtAt, String builtBy, String sha256, String signature, boolean multiTenant) {}
        
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
        
        TestManifest dummy = new TestManifest("1.0", "template", "0.1.0", Instant.now(), "test", "DUMMY", "fake", true);
        writeZip(dbpkg, MAPPER.writeValueAsString(dummy), moduleGraph, apiSchema);
        
        byte[] bytes = Files.readAllBytes(dbpkg);
        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        TestManifest real = new TestManifest("1.0", "template", "0.1.0", dummy.builtAt(), "test", sha, "fake", true);
        writeZip(dbpkg, MAPPER.writeValueAsString(real), moduleGraph, apiSchema);
    }
    
    private void writeZip(Path dbpkg, String manifest, String moduleGraph, String apiSchema) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dbpkg.toFile()))) {
            writeEntry(zos, "manifest.json", manifest);
            writeEntry(zos, "runtime/module_graph.json", moduleGraph);
            writeEntry(zos, "contracts/APISchema.json", apiSchema);
            writeDir(zos, "contracts/"); 
            writeDir(zos, "workflows/"); 
            writeDir(zos, "projections/"); 
            writeDir(zos, "runtime/"); 
            writeDir(zos, "bootstrap/");
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
