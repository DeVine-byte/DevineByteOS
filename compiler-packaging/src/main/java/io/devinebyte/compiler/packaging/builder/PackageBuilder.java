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
import io.devinebyte.runtime.plugin.PluginManifest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
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

        // Pass 1: write with placeholder checksum
        Manifest placeholder = createManifest(content, "sha256-placeholder", List.of());
        writeZip(tempPath, content, placeholder);

        String checksum = ChecksumUtil.sha256(tempPath);

        // Pass 2: collect plugin hashes from temp, then write final
        List<PluginManifest.PluginEntry> pluginEntries = collectPluginEntries(content);
        Manifest real = createManifest(content, checksum, pluginEntries);
        writeZip(finalPath, content, real);

        Files.delete(tempPath);
        System.out.println("Built: " + finalPath + " SHA256=" + checksum);
        return finalPath;
    }

    private Manifest createManifest(PackageContent content, String checksum, List<PluginManifest.PluginEntry> plugins) {
        Map<String, Object> metadata = new LinkedHashMap<>(content.metadata());
        metadata.put("plugins", plugins);

        return new Manifest(
            "1.0",
            content.tenant().tenantId(),
            content.version(),
            Instant.now(),
            "devinebyte-compiler-1.0.0",
            checksum,
            "", // signature added later
            content.moduleGraph(),
            Map.of("contracts", "4"),
            content.multiTenant(),
            metadata
        );
    }

    private void writeZip(Path zipPath, PackageContent content, Manifest manifest) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            // directories
            zos.putNextEntry(new ZipEntry("contracts/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("workflows/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("projections/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("runtime/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("bootstrap/")); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("bootstrap/plugins/")); zos.closeEntry();

            // contracts
            writeJson(zos, "contracts/EventSchema.json", content.eventSchemas());
            writeJson(zos, "contracts/EntitySchema.json", content.entitySchemas());
            writeJson(zos, "contracts/WorkflowSchema.json", content.workflowSchemas());
            writeJson(zos, "contracts/APISchema.json", content.apiSchemas());

            // workflows + projections
            writeJson(zos, "workflows/compiled_state_machines.json", content.workflows());
            writeJson(zos, "projections/dashboard_definitions.json", content.dashboards());

            for (int i = 0; i < content.projections().size(); i++) {
                writeBytes(zos, "projections/projection_" + i + ".wasm",
                    content.projections().get(i).toString().getBytes());
            }

            // runtime
            writeJson(zos, "runtime/tenant_config.json", content.tenantConfig());
            writeJson(zos, "runtime/feature_flags.json", content.featureFlags());
            writeModuleGraph(zos, content);

            // bootstrap
            writeBytes(zos, "bootstrap/runtime_bootstrap.class", content.runtimeBootstrapClass());

            // plugins
            List<PluginManifest.PluginEntry> pluginEntries = writePlugins(zos, content);
            writeJson(zos, "bootstrap/plugins/manifest.json", new PluginManifest(pluginEntries));

            // manifest last
            writeManifest(zos, manifest);
        }
    }

    private List<PluginManifest.PluginEntry> writePlugins(ZipOutputStream zos, PackageContent content) throws IOException {
        List<PluginManifest.PluginEntry> entries = new ArrayList<>();
        Path pluginsSourceDir = content.pluginsDir();

        if (pluginsSourceDir!= null && Files.exists(pluginsSourceDir)) {
            try (var stream = Files.list(pluginsSourceDir)) {
                for (Path jar : stream.filter(p -> p.toString().endsWith(".jar")).sorted().toList()) {
                    String jarName = jar.getFileName().toString();
                    byte[] jarBytes = Files.readAllBytes(jar);
                    String sha256 = ChecksumUtil.sha256(jarBytes);

                    writeBytes(zos, "bootstrap/plugins/" + jarName, jarBytes);

                    String entrypoint = readEntrypointFromJar(jarBytes).orElse("com.devinebyte.plugin.PluginImpl");
                    String id = jarName.split("-")[0];
                    String version = extractVersion(jarName);
                    String moduleId = id.toUpperCase(Locale.ROOT);

                    entries.add(new PluginManifest.PluginEntry(
                        id, version, jarName, entrypoint, "0.1", moduleId, sha256
                    ));
                }
            }
        }
        return entries;
    }

    private List<PluginManifest.PluginEntry> collectPluginEntries(PackageContent content) throws IOException {
        List<PluginManifest.PluginEntry> entries = new ArrayList<>();
        Path pluginsSourceDir = content.pluginsDir();
        if (pluginsSourceDir!= null && Files.exists(pluginsSourceDir)) {
            try (var stream = Files.list(pluginsSourceDir)) {
                for (Path jar : stream.filter(p -> p.toString().endsWith(".jar")).sorted().toList()) {
                    byte[] jarBytes = Files.readAllBytes(jar);
                    String sha256 = ChecksumUtil.sha256(jarBytes);
                    String jarName = jar.getFileName().toString();
                    String entrypoint = readEntrypointFromJar(jarBytes).orElse("com.devinebyte.plugin.PluginImpl");
                    String id = jarName.split("-")[0];
                    entries.add(new PluginManifest.PluginEntry(
                        id, extractVersion(jarName), jarName, entrypoint, "0.1", id.toUpperCase(Locale.ROOT), sha256
                    ));
                }
            }
        }
        return entries;
    }

    private Optional<String> readEntrypointFromJar(byte[] jarBytes) throws IOException {
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes))) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry())!= null) {
                if (entry.getName().equals("META-INF/services/io.devinebyte.runtime.plugin.RuntimePlugin")) {
                    String service = new String(jis.readAllBytes()).trim();
                    return Optional.of(service.split("\n")[0].trim());
                }
            }
        }
        return Optional.empty();
    }

    private String extractVersion(String jarName) {
        return jarName.replaceAll(".*-(\\d+\\.\\d+\\.\\d+)\\.jar", "$1");
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
