package io.devinebyte.compiler.packaging.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.packaging.model.Manifest;
import io.devinebyte.compiler.packaging.model.PackageContent;
import io.devinebyte.compiler.packaging.builder.ChecksumUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
            "1.0",
            content.tenant().tenantId(),
            content.version(),
            Instant.now(),
            "devinebyte-compiler-1.0.0",
            checksum,
            "",
            content.moduleGraph(),
            Map.of("contracts", "4"),
            content.multiTenant(),
            Map.of()
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
            writeModuleGraph(zos, content);
            writeBytes(zos, "bootstrap/runtime_bootstrap.class", content.runtimeBootstrapClass());
            writeManifest(zos, manifest);
        }
    }

    // KEY FIX: Case-insensitive dedupe + case-insensitive enabled check
    private void writeModuleGraph(ZipOutputStream zos, PackageContent content) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode modulesNode = mapper.createObjectNode();

        Set<String> enabledLower = content.tenant().enabledModules().stream()
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        Map<String, ModuleIR> modulesByLower = new LinkedHashMap<>();
        for (ModuleIR m : content.blueprint().modules()) {
            modulesByLower.putIfAbsent(m.name().toLowerCase(Locale.ROOT), m);
        }

        for (ModuleIR m : modulesByLower.values()) {
            String id = m.name();
            String idLower = id.toLowerCase(Locale.ROOT);

            ObjectNode modNode = mapper.createObjectNode();
            modNode.put("moduleId", id);
            modNode.put("enabled", enabledLower.contains(idLower));

            ArrayNode deps = mapper.createArrayNode();
            for (String d : m.dependencies()) {
                String depCanonical = modulesByLower.get(d.toLowerCase(Locale.ROOT)).name();
                deps.add(depCanonical);
            }
            modNode.set("dependsOn", deps);

            ArrayNode exposes = mapper.createArrayNode();
            m.events().forEach(e -> exposes.add(e.name()));
            modNode.set("exposesEvents", exposes);
            modNode.set("subscribesToEvents", mapper.createArrayNode());

            modulesNode.set(id, modNode);
        }

        root.set("modules", modulesNode);
        writeJson(zos, "runtime/module_graph.json", root);
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
        map.put("multiTenant", manifest.multiTenant());
        map.put("keywordAliases", manifest.keywordAliases());
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
