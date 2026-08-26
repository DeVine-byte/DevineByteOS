package io.devinebyte.runtime.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.HexFormat;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader {
    private final Path pluginsPath; // /bootstrap/plugins from extracted dbpkg
    private final ObjectMapper mapper = new ObjectMapper();

    public PluginLoader(Path pluginsPath) {
        this.pluginsPath = pluginsPath;
    }

    public List<RuntimePlugin> loadPlugins(PluginContext context, DiagnosticCollector diagnostics) {
        List<RuntimePlugin> loaded = new ArrayList<>();

        Path manifestPath = pluginsPath.resolve("manifest.json");
        if (!Files.exists(manifestPath)) {
            diagnostics.addInfo("BOOT_010", "No plugins found");
            return loaded;
        }

        PluginManifest manifest = readManifest(manifestPath, diagnostics);
        if (manifest == null) return loaded;

        for (PluginManifest.PluginEntry entry : manifest.plugins()) {
            Path jarPath = pluginsPath.resolve(entry.artifact());
            if (!Files.exists(jarPath)) {
                diagnostics.add("DBRT151", "Plugin jar missing: " + jarPath);
                continue;
            }
            if (!verifySha256(jarPath, entry.sha256())) {
                diagnostics.add("DBRT004", "Plugin signature invalid: " + entry.id());
                continue;
            }

            try (URLClassLoader cl = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, getClass().getClassLoader())) {
                Class<?> pluginClass = cl.loadClass(entry.entrypoint());
                if (!RuntimePlugin.class.isAssignableFrom(pluginClass)) {
                    diagnostics.add("DBRT152", "Entrypoint does not implement RuntimePlugin: " + entry.entrypoint());
                    continue;
                }
                RuntimePlugin plugin = (RuntimePlugin) pluginClass.getDeclaredConstructor().newInstance();
                loaded.add(plugin);
                diagnostics.addInfo("BOOT_011", "Loaded plugin: " + entry.id() + "@" + entry.version());
            } catch (Exception e) {
                diagnostics.add("DBRT150", "Failed to load plugin: " + entry.id() + " - " + e.getMessage());
            }
        }
        return loaded;
    }

    private PluginManifest readManifest(Path manifestPath, DiagnosticCollector diagnostics) {
        try {
            return mapper.readValue(manifestPath.toFile(), PluginManifest.class);
        } catch (IOException e) {
            diagnostics.fatal("DBRT150", "Failed to read plugin manifest: " + e.getMessage());
            return null;
        }
    }

    private boolean verifySha256(Path jarPath, String expected) {
        if (expected == null || expected.isBlank()) return true; // skip if not provided
        try {
            byte[] bytes = Files.readAllBytes(jarPath);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            String actual = HexFormat.of().formatHex(hash);
            return actual.equalsIgnoreCase(expected);
        } catch (Exception e) { 
            return false; 
        }
    }
}
