package io.devinebyte.compiler.packaging.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.packaging.model.PackageContent;
import io.devinebyte.runtime.plugin.PluginManifest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackageBuilder {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Pattern SEMVER_PATTERN =
            Pattern.compile("^\\d+\\.\\d+\\.\\d+(?:-[a-zA-Z0-9.]+)?$");

    private record FinalProductionManifest(
            String schemaVersion,
            String tenantId,
            String version,
            Instant builtAt,
            String builtBy,
            @JsonProperty("sha256") String checksumSha256,
            String signature,
            boolean multiTenant,
            Map<String, String> metadata,
            Map<String, String> keywordAliases,
            List<String> enabledModules,
            String minRuntimeVersion,
            Map<String, String> dependencies,
            Map<String, Boolean> features
    ) {}

    public Path build(PackageContent content, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);

        String semanticVersion = content.version();
        if (semanticVersion == null || !SEMVER_PATTERN.matcher(semanticVersion).matches()) {
            throw new IllegalArgumentException(
                    "Packaging Error: Provided version parameter string '" + semanticVersion +
                    "' violates strict Semantic Versioning rules."
            );
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IOException(
                    "Critical Cryptographic Error: SHA-256 Engine instantiation denied.",
                    e
            );
        }

        DigestOutputStream digestStream = new DigestOutputStream(byteStream, digest);
        Map<String, byte[]> sortedPayloadEntries = new TreeMap<>();
        collectImmutablePayloadAssetStates(content, sortedPayloadEntries);

        try (ZipOutputStream zos = new ZipOutputStream(digestStream)) {
            writeDirectoryNode(zos, "contracts/");
            writeDirectoryNode(zos, "workflows/");
            writeDirectoryNode(zos, "projections/");
            writeDirectoryNode(zos, "runtime/");
            writeDirectoryNode(zos, "bootstrap/");
            writeDirectoryNode(zos, "bootstrap/plugins/");

            for (Map.Entry<String, byte[]> entry : sortedPayloadEntries.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }

        byte[] rawContentHash = digest.digest();
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : rawContentHash) {
            hexBuilder.append(String.format("%02x", b));
        }
        String contentSha256 = hexBuilder.toString();

        FinalProductionManifest manifest = new FinalProductionManifest(
                "2.0",
                content.tenant().tenantId(),
                semanticVersion,
                Instant.now(),
                "devinebyte-compiler-sdk",
                contentSha256,
                "secure-ed25519-signature-placeholder",
                true,
                Map.of(),
                Map.of(),
                new ArrayList<>(content.tenant().enabledModules()),
                "1.0.0",
                Map.of("core-substrate", "1.0.0"),
                Map.of("telemetry.enabled", true)
        );

        ByteArrayOutputStream finalOutStream = new ByteArrayOutputStream();
        try (ZipOutputStream finalZos = new ZipOutputStream(finalOutStream)) {
            writeDirectoryNode(finalZos, "contracts/");
            writeDirectoryNode(finalZos, "workflows/");
            writeDirectoryNode(finalZos, "projections/");
            writeDirectoryNode(finalZos, "runtime/");
            writeDirectoryNode(finalZos, "bootstrap/");
            writeDirectoryNode(finalZos, "bootstrap/plugins/");

            for (Map.Entry<String, byte[]> entry : sortedPayloadEntries.entrySet()) {
                finalZos.putNextEntry(new ZipEntry(entry.getKey()));
                finalZos.write(entry.getValue());
                finalZos.closeEntry();
            }

            finalZos.putNextEntry(new ZipEntry("manifest.json"));
            finalZos.write(
                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsBytes(manifest)
            );
            finalZos.closeEntry();
        }

        String fileName = String.format(
                "tenant-%s-v%s.dbpkg",
                content.tenant().tenantId(),
                semanticVersion
        );
        Path finalPath = outputDir.resolve(fileName);
        Files.write(finalPath, finalOutStream.toByteArray());
        return finalPath;
    }

    private void collectImmutablePayloadAssetStates(
            PackageContent content,
            Map<String, byte[]> payloadMap
    ) throws IOException {
        payloadMap.put(
                "contracts/EventSchema.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.eventSchemas())
        );
        payloadMap.put(
                "contracts/EntitySchema.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.entitySchemas())
        );
        payloadMap.put(
                "contracts/WorkflowSchema.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.workflowSchemas())
        );
        payloadMap.put(
                "contracts/APISchema.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.apiSchemas())
        );
        payloadMap.put(
                "workflows/compiled_state_machines.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.workflows())
        );
        payloadMap.put(
                "projections/dashboard_definitions.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.dashboards())
        );

        for (int i = 0; i < content.projections().size(); i++) {
            payloadMap.put(
                    "projections/projection_" + i + ".wasm",
                    content.projections().get(i)
                            .toString()
                            .getBytes(StandardCharsets.UTF_8)
            );
        }

        payloadMap.put(
                "runtime/tenant_config.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.tenantConfig())
        );
        payloadMap.put(
                "runtime/feature_flags.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(content.featureFlags())
        );

        ObjectNode root = mapper.createObjectNode();
        ObjectNode modulesNode = mapper.createObjectNode();

        Set<String> enabledLower = content.tenant().enabledModules().stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, ModuleIR> modulesByLower = new LinkedHashMap<>();
        for (ModuleIR module : content.blueprint().modules()) {
            modulesByLower.putIfAbsent(
                    module.name().toLowerCase(Locale.ROOT),
                    module
            );
        }

        for (ModuleIR module : modulesByLower.values()) {
            String id = module.name();
            String idLower = id.toLowerCase(Locale.ROOT);

            ObjectNode modNode = mapper.createObjectNode();
            modNode.put("moduleId", id);
            modNode.put("enabled", enabledLower.contains(idLower));

            ArrayNode deps = mapper.createArrayNode();
            for (String dependency : module.dependencies()) {
                ModuleIR dependencyModule = modulesByLower.get(
                        dependency.toLowerCase(Locale.ROOT)
                );
                if (dependencyModule != null) {
                    deps.add(dependencyModule.name());
                }
            }
            modNode.set("dependsOn", deps);

            ArrayNode exposes = mapper.createArrayNode();
            module.events().forEach(event -> exposes.add(event.name()));
            modNode.set("exposesEvents", exposes);
            modNode.set("subscribesToEvents", mapper.createArrayNode());

            modulesNode.set(id, modNode);
        }

        root.set("modules", modulesNode);
        payloadMap.put(
                "runtime/module_graph.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(root)
        );

        payloadMap.put(
                "bootstrap/runtime_bootstrap.class",
                content.runtimeBootstrapClass()
        );

        List<PluginManifest.PluginEntry> pluginEntries =
                collectAndWritePlugins(content, payloadMap);

        payloadMap.put(
                "bootstrap/plugins/manifest.json",
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(new PluginManifest(pluginEntries))
        );
    }

    private List<PluginManifest.PluginEntry> collectAndWritePlugins(
            PackageContent content,
            Map<String, byte[]> payloadMap
    ) throws IOException {
        List<PluginManifest.PluginEntry> entries = new ArrayList<>();
        Path pluginsSourceDir = content.pluginsDir();

        if (pluginsSourceDir == null || !Files.exists(pluginsSourceDir)) {
            return entries;
        }

        Set<String> enabledModules = getEnabledModules(content);

        try (var stream = Files.list(pluginsSourceDir)) {
            for (Path jar : stream
                    .filter(p -> p.toString().endsWith(".jar"))
                    .sorted()
                    .toList()) {

                String jarName = jar.getFileName().toString();
                String jarModuleId = jarName
                        .split("-")[0]
                        .toLowerCase(Locale.ROOT);

                if (!enabledModules.contains(jarModuleId)) {
                    continue;
                }

                byte[] jarBytes = Files.readAllBytes(jar);
                MessageDigest jarDigest;

                try {
                    jarDigest = MessageDigest.getInstance("SHA-256");
                } catch (Exception e) {
                    throw new IOException(e);
                }

                byte[] jarHash = jarDigest.digest(jarBytes);
                StringBuilder sb = new StringBuilder();

                for (byte b : jarHash) {
                    sb.append(String.format("%02x", b));
                }

                String sha256 = sb.toString();
                payloadMap.put("bootstrap/plugins/" + jarName, jarBytes);

                String entrypoint = readEntrypointFromJar(jarBytes)
                        .orElse("com.devinebyte.plugin.PluginImpl");

                String id = jarName.split("-")[0];
                String version = extractVersion(jarName);
                String moduleId = id.toUpperCase(Locale.ROOT);

                entries.add(new PluginManifest.PluginEntry(
                        id,
                        version,
                        jarName,
                        entrypoint,
                        "0.1",
                        moduleId,
                        sha256
                ));
            }
        }

        return entries;
    }

    private void writeDirectoryNode(
            ZipOutputStream zos,
            String name
    ) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.closeEntry();
    }

    private Set<String> getEnabledModules(PackageContent content) {
        return content.blueprint().modules().stream()
                .filter(module -> content.tenant().enabledModules().stream()
                        .anyMatch(enabled ->
                                enabled.equalsIgnoreCase(module.name())))
                .map(module ->
                        module.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Optional<String> readEntrypointFromJar(
            byte[] jarBytes
    ) throws IOException {
        try (JarInputStream jis =
                     new JarInputStream(new ByteArrayInputStream(jarBytes))) {

            JarEntry entry;

            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.getName().equals(
                        "META-INF/services/io.devinebyte.runtime.plugin.RuntimePlugin"
                )) {
                    String service = new String(
                            jis.readAllBytes(),
                            StandardCharsets.UTF_8
                    ).trim();

                    if (!service.isEmpty()) {
                        return Optional.of(
                                service.split("\n")[0].trim()
                        );
                    }
                }
            }
        }

        return Optional.empty();
    }

    private String extractVersion(String jarName) {
        return jarName.replaceAll(
                ".*-(\\d+\\.\\d+\\.\\d+)\\.jar",
                "$1"
        );
    }
}
