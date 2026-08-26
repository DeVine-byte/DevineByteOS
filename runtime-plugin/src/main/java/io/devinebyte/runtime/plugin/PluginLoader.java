package io.devinebyte.runtime.plugin;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;

public class PluginLoader {
    private final Path pluginsPath; // /bootstrap/plugins from extracted dbpkg

    public PluginLoader(Path pluginsPath) {
        this.pluginsPath = pluginsPath;
    }

    public List<RuntimePlugin> loadPlugins(PluginContext context, DiagnosticCollector diagnostics) {
        List<RuntimePlugin> loaded = new ArrayList<>();

        Path manifestPath = pluginsPath.resolve("manifest.json");
        if (!Files.exists(manifestPath)) return loaded;

        PluginManifest manifest = readManifest(manifestPath);

        for (PluginManifest.PluginEntry entry : manifest.plugins()) {
            Path jarPath = pluginsPath.resolve(entry.artifact());
            if (!verifySha256(jarPath, entry.sha256())) {
                diagnostics.add("DBRT004", "Plugin signature invalid: " + entry.id());
                continue;
            }

            try (URLClassLoader cl = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, getClass().getClassLoader())) {
                Class<?> pluginClass = cl.loadClass(entry.entrypoint());
                RuntimePlugin plugin = (RuntimePlugin) pluginClass.getDeclaredConstructor().newInstance();
                loaded.add(plugin);
            } catch (Exception e) {
                diagnostics.add("DBRT150", "Failed to load plugin: " + entry.id());
            }
        }
        return loaded;
    }
    public class DbpkgExtractor {
        public static void extract(ZipFile zip, Path dest, DiagnosticCollector diagnostics) { ... }
    }
}
