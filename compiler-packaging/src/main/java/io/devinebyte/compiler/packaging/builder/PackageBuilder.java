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

    public Path build(
            PackageContent content,
            Path outputDir
    ) throws IOException {

        Files.createDirectories(outputDir);

        String fileName =
                "tenant-"
                        + content.tenant().tenantId()
                        + "-v"
                        + content.version()
                        + ".dbpkg";

        Path tempPath =
                outputDir.resolve("temp-" + fileName);

        Path finalPath =
                outputDir.resolve(fileName);

        // Pass 1: write with placeholder checksum
        Manifest placeholder =
                createManifest(
                        content,
                        "sha256-placeholder",
                        List.of()
                );

        writeZip(
                tempPath,
                content,
                placeholder
        );

        String checksum =
                ChecksumUtil.sha256(tempPath);

        // Pass 2: collect plugin hashes from source,
        // then write final package
        List<PluginManifest.PluginEntry> pluginEntries =
                collectPluginEntries(content);

        Manifest real =
                createManifest(
                        content,
                        checksum,
                        pluginEntries
                );

        writeZip(
                finalPath,
                content,
                real
        );

        Files.delete(tempPath);

        System.out.println(
                "Built: "
                        + finalPath
                        + " SHA256="
                        + checksum
        );

        return finalPath;
    }

    private Manifest createManifest(
            PackageContent content,
            String checksum,
            List<PluginManifest.PluginEntry> plugins
    ) {

        // Strictly typed metadata to align with
        // Manifest constructor requirements.
        Map<String, String> metadata =
                new LinkedHashMap<>();

        if (content.tenantConfig() != null) {
            metadata.putAll(
                    content.tenantConfig()
            );
        }

        if (content.featureFlags() != null) {
            content.featureFlags().forEach(
                    (k, v) ->
                            metadata.put(
                                    k,
                                    String.valueOf(v)
                            )
            );
        }

        try {
            // Serialize plugins as JSON inside the
            // String/String metadata map.
            metadata.put(
                    "plugins",
                    mapper.writeValueAsString(plugins)
            );
        } catch (IOException e) {
            metadata.put(
                    "plugins",
                    "[]"
            );
        }

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

    private void writeZip(
            Path zipPath,
            PackageContent content,
            Manifest manifest
    ) throws IOException {

        try (ZipOutputStream zos =
                     new ZipOutputStream(
                             Files.newOutputStream(zipPath)
                     )) {

            // -------------------------------------------------
            // Directories
            // -------------------------------------------------

            zos.putNextEntry(
                    new ZipEntry("contracts/")
            );
            zos.closeEntry();

            zos.putNextEntry(
                    new ZipEntry("workflows/")
            );
            zos.closeEntry();

            zos.putNextEntry(
                    new ZipEntry("projections/")
            );
            zos.closeEntry();

            zos.putNextEntry(
                    new ZipEntry("runtime/")
            );
            zos.closeEntry();

            zos.putNextEntry(
                    new ZipEntry("bootstrap/")
            );
            zos.closeEntry();

            zos.putNextEntry(
                    new ZipEntry("bootstrap/plugins/")
            );
            zos.closeEntry();

            // -------------------------------------------------
            // Contracts
            // -------------------------------------------------

            writeJson(
                    zos,
                    "contracts/EventSchema.json",
                    content.eventSchemas()
            );

            writeJson(
                    zos,
                    "contracts/EntitySchema.json",
                    content.entitySchemas()
            );

            writeJson(
                    zos,
                    "contracts/WorkflowSchema.json",
                    content.workflowSchemas()
            );

            writeJson(
                    zos,
                    "contracts/APISchema.json",
                    content.apiSchemas()
            );

            // -------------------------------------------------
            // Workflows + projections
            // -------------------------------------------------

            writeJson(
                    zos,
                    "workflows/compiled_state_machines.json",
                    content.workflows()
            );

            writeJson(
                    zos,
                    "projections/dashboard_definitions.json",
                    content.dashboards()
            );

            for (int i = 0;
                 i < content.projections().size();
                 i++) {

                writeBytes(
                        zos,
                        "projections/projection_"
                                + i
                                + ".wasm",
                        content.projections()
                                .get(i)
                                .toString()
                                .getBytes()
                );
            }

            // -------------------------------------------------
            // Runtime
            // -------------------------------------------------

            writeJson(
                    zos,
                    "runtime/tenant_config.json",
                    content.tenantConfig()
            );

            writeJson(
                    zos,
                    "runtime/feature_flags.json",
                    content.featureFlags()
            );

            writeModuleGraph(
                    zos,
                    content
            );

            // -------------------------------------------------
            // Bootstrap
            // -------------------------------------------------

            writeBytes(
                    zos,
                    "bootstrap/runtime_bootstrap.class",
                    content.runtimeBootstrapClass()
            );

            // -------------------------------------------------
            // Plugins
            // -------------------------------------------------

            List<PluginManifest.PluginEntry> pluginEntries =
                    writePlugins(
                            zos,
                            content
                    );

            writeJson(
                    zos,
                    "bootstrap/plugins/manifest.json",
                    new PluginManifest(pluginEntries)
            );

            // -------------------------------------------------
            // Manifest
            // -------------------------------------------------

            writeManifest(
                    zos,
                    manifest
            );
        }
    }

    /**
     * Writes only plugins belonging to modules that are
     * actually enabled in the tenant blueprint.
     */
    private List<PluginManifest.PluginEntry> writePlugins(
            ZipOutputStream zos,
            PackageContent content
    ) throws IOException {

        List<PluginManifest.PluginEntry> entries =
                new ArrayList<>();

        Path pluginsSourceDir =
                content.pluginsDir();

        if (pluginsSourceDir == null
                || !Files.exists(pluginsSourceDir)) {

            return entries;
        }

        // Build set of enabled module names from Blueprint.
        //
        // Matching is case-insensitive.
        Set<String> enabledModules =
                content.blueprint()
                        .modules()
                        .stream()
                        .filter(
                                m ->
                                        content.tenant()
                                                .enabledModules()
                                                .stream()
                                                .anyMatch(
                                                        em ->
                                                                em.equalsIgnoreCase(
                                                                        m.name()
                                                                )
                                                )
                        )
                        .map(
                                m ->
                                        m.name()
                                                .toLowerCase(
                                                        Locale.ROOT
                                                )
                        )
                        .collect(
                                Collectors.toSet()
                        );

        try (var stream =
                     Files.list(pluginsSourceDir)) {

            for (Path jar : stream
                    .filter(
                            p ->
                                    p.toString()
                                            .endsWith(".jar")
                    )
                    .sorted()
                    .toList()) {

                String jarName =
                        jar.getFileName()
                                .toString();

                String jarModuleId =
                        jarName
                                .split("-")[0]
                                .toLowerCase(
                                        Locale.ROOT
                                );

                // Only load the plugin if its module
                // is enabled for this tenant.
                if (!enabledModules.contains(
                        jarModuleId
                )) {

                    System.out.println(
                            "[PKG] Skipping plugin "
                                    + jarName
                                    + " - module not enabled"
                    );

                    continue;
                }

                byte[] jarBytes =
                        Files.readAllBytes(jar);

                String sha256 =
                        ChecksumUtil.sha256(
                                jarBytes
                        );

                writeBytes(
                        zos,
                        "bootstrap/plugins/"
                                + jarName,
                        jarBytes
                );

                String entrypoint =
                        readEntrypointFromJar(
                                jarBytes
                        ).orElse(
                                "com.devinebyte.plugin.PluginImpl"
                        );

                String id =
                        jarName.split("-")[0];

                String version =
                        extractVersion(jarName);

                String moduleId =
                        id.toUpperCase(
                                Locale.ROOT
                        );

                entries.add(
                        new PluginManifest.PluginEntry(
                                id,
                                version,
                                jarName,
                                entrypoint,
                                "0.1",
                                moduleId,
                                sha256
                        )
                );

                System.out.println(
                        "[PKG] Loaded plugin "
                                + jarName
                                + " for module "
                                + moduleId
                );
            }
        }

        return entries;
    }

    /**
     * Collects plugin entries using the exact same
     * module enablement rules used by writePlugins().
     *
     * This ensures Pass 1 and Pass 2 produce the same
     * plugin manifest.
     */
    private List<PluginManifest.PluginEntry> collectPluginEntries(
            PackageContent content
    ) throws IOException {

        List<PluginManifest.PluginEntry> entries =
                new ArrayList<>();

        Path pluginsSourceDir =
                content.pluginsDir();

        if (pluginsSourceDir == null
                || !Files.exists(pluginsSourceDir)) {

            return entries;
        }

        // Synchronized Filter Step:
        // Exact same module enablement checks as pass 1.
        Set<String> enabledModules =
                content.blueprint()
                        .modules()
                        .stream()
                        .filter(
                                m ->
                                        content.tenant()
                                                .enabledModules()
                                                .stream()
                                                .anyMatch(
                                                        em ->
                                                                em.equalsIgnoreCase(
                                                                        m.name()
                                                                )
                                                )
                        )
                        .map(
                                m ->
                                        m.name()
                                                .toLowerCase(
                                                        Locale.ROOT
                                                )
                        )
                        .collect(
                                Collectors.toSet()
                        );

        try (var stream =
                     Files.list(pluginsSourceDir)) {

            for (Path jar : stream
                    .filter(
                            p ->
                                    p.toString()
                                            .endsWith(".jar")
                    )
                    .sorted()
                    .toList()) {

                String jarName =
                        jar.getFileName()
                                .toString();

                String jarModuleId =
                        jarName
                                .split("-")[0]
                                .toLowerCase(
                                        Locale.ROOT
                                );

                if (!enabledModules.contains(
                        jarModuleId
                )) {

                    // Skip silently on pass 2.
                    continue;
                }

                byte[] jarBytes =
                        Files.readAllBytes(jar);

                String sha256 =
                        ChecksumUtil.sha256(
                                jarBytes
                        );

                String entrypoint =
                        readEntrypointFromJar(
                                jarBytes
                        ).orElse(
                                "com.devinebyte.plugin.PluginImpl"
                        );

                String id =
                        jarName.split("-")[0];

                String version =
                        extractVersion(jarName);

                String moduleId =
                        id.toUpperCase(
                                Locale.ROOT
                        );

                entries.add(
                        new PluginManifest.PluginEntry(
                                id,
                                version,
                                jarName,
                                entrypoint,
                                "0.1",
                                moduleId,
                                sha256
                        )
                );
            }
        }

        return entries;
    }

    private Optional<String> readEntrypointFromJar(
            byte[] jarBytes
    ) throws IOException {

        try (JarInputStream jis =
                     new JarInputStream(
                             new ByteArrayInputStream(
                                     jarBytes
                             )
                     )) {

            JarEntry entry;

            while (
                    (entry = jis.getNextJarEntry())
                            != null
            ) {

                if (entry.getName().equals(
                        "META-INF/services/"
                                + "io.devinebyte.runtime.plugin.RuntimePlugin"
                )) {

                    String service =
                            new String(
                                    jis.readAllBytes()
                            ).trim();

                    return Optional.of(
                            service.split("\n")[0]
                                    .trim()
                    );
                }
            }
        }

        return Optional.empty();
    }

    private String extractVersion(
            String jarName
    ) {

        return jarName.replaceAll(
                ".*-(\\d+\\.\\d+\\.\\d+)\\.jar",
                "$1"
        );
    }

    // Case-insensitive dedupe +
    // case-insensitive enabled check.
    private void writeModuleGraph(
            ZipOutputStream zos,
            PackageContent content
    ) throws IOException {

        ObjectNode root =
                mapper.createObjectNode();

        ObjectNode modulesNode =
                mapper.createObjectNode();

        Set<String> enabledLower =
                content.tenant()
                        .enabledModules()
                        .stream()
         
