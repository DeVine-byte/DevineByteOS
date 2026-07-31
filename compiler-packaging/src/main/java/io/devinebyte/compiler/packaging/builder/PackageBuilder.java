package io.devinebyte.compiler.packaging.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.devinebyte.compiler.packaging.model.Manifest;
import io.devinebyte.compiler.packaging.model.PackageContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackageBuilder {
    private final ObjectMapper mapper = new ObjectMapper()
       .registerModule(new JavaTimeModule())
       .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public Path build(PackageContent content, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        String fileName = "tenant-" + content.tenant().tenantId() + "-v" + content.version() + ".dbpkg";
        Path tempPath = outputDir.resolve("temp-" + fileName);
        Path finalPath = outputDir.resolve(fileName);

        Manifest placeholder = createManifest(content, "sha256-placeholder");
        writeZip(tempPath, content, placeholder);

        String checksum = ChecksumUtil.sha256(tempPath);

        Manifest real = createManifest(content, checksum);
        writeZip(finalPath, content, real);

        Files.delete(tempPath);
        System.out.println("Built: " + finalPath + " SHA256=" + checksum);
        return finalPath;
    }

    private Manifest createManifest(PackageContent content, String checksum) {
        return new Manifest(
            "1.0", // schemaVersion
            content.tenant().tenantId(), // tenantId
            content.version(), // version
            Instant.now(), // builtAt
            "devinebyte-compiler-1.0.0", // builtBy
            checksum, // sha256
            "", // signature
            content.moduleGraph(),
            Map.of("contracts", "4"),
            content.multiTenant() // NEW: Pass multiTenant
        );
    }

    private void writeZip(Path zipPath, PackageContent content, Manifest manifest) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            zos.putNextEntry(new ZipEntry("contracts/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("workflows/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("projections/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("runtime/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("bootstrap/")); zos.closeEntry();

            writeJson(zos, "contracts/EventSchema.json", content.eventSchemas());
            writeJson(zos, "contracts/EntitySchema.json", content.entitySchemas());
            writeJson(zos, "contracts/WorkflowSchema.json", content.workflowSchemas());
            writeJson(zos, "contracts/APISchema.json", content.apiSchemas());
            writeJson(zos, "workflows/compiled_state_machines.json", content.workflows());
            writeJson(zos, "projections/dashboard_definitions.json", content.dashboards());

            for (int i = 0; i < content.projections().size(); i++) {
                writeBytes(zos, "projections/projection_" + i + ".wasm",
                    content.projections().get(i).toString().getBytes());
            }

            writeJson(zos, "runtime/tenant_config.json", content.tenantConfig());
            writeJson(zos, "runtime/feature_flags.json", content.featureFlags());
            writeJson(zos, "runtime/module_graph.json", content.moduleGraph());
            writeBytes(zos, "bootstrap/runtime_bootstrap.class", content.runtimeBootstrapClass());

            writeManifest(zos, manifest);
        }
    }

    private void writeManifest(ZipOutputStream zos, Manifest manifest) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("schemaVersion", manifest.schemaVersion());
        map.put("tenantId", manifest.tenantId());
        map.put("version", manifest.version());
        map.put("builtAt", manifest.builtAt().toString());
        map.put("builtBy", manifest.builtBy());
        map.put("sha256", manifest.sha256());
        map.put("signature", manifest.signature());
        map.put("moduleGraph", manifest.moduleGraph());
        map.put("metadata", manifest.metadata());
        map.put("multiTenant", manifest.multiTenant()); // NEW: Write to manifest.json

        writeJson(zos, "manifest.json", map);
    }

    private void writeJson(ZipOutputStream zos, String path, Object obj) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(obj));
        zos.closeEntry();
    }

    private void writeBytes(ZipOutputStream zos, String path, byte[] data) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(data);
        zos.closeEntry();
    }
}
