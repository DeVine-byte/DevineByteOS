package io.devinebyte.runtime.module;

import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import jakarta.inject.Singleton;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Singleton
public final class DynamicPluginJarLoader {

    private static final String MODULE_BASE_PATH = "modules/%s/";

    public List<Class<?>> loadPluginComponents(TenantContext tenant, List<ModuleDefinition> loadOrder, DiagnosticCollector diagnostics) {
        List<Class<?>> loadedClasses = new ArrayList<>();
        String tenantId = tenant.tenantId();

        for (ModuleDefinition def : loadOrder) {
            String moduleId = def.moduleId(); // FIXED: Uses .moduleId() to match your core ModuleGraph definition model properties
            String targetFolderPath = String.format(MODULE_BASE_PATH, moduleId);
            File folder = new File(targetFolderPath);

            if (!folder.exists() || !folder.isDirectory()) {
                folder = new File("../" + targetFolderPath);
            }

            if (!folder.exists() || folder.listFiles() == null) {
                continue;
            }

            File[] targetFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (targetFiles == null || targetFiles.length == 0) {
                continue;
            }

            for (File jarFile : targetFiles) {
                System.out.println("[PLUGIN CONVENTION] Loading extension package -> " + jarFile.getAbsolutePath());
                try {
                    URL[] urls = new URL[]{ jarFile.toURI().toURL() };
                    try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
                        String expectedClassName = "io.devinebyte.plugin." + moduleId + "." + 
                            moduleId.substring(0, 1).toUpperCase() + moduleId.substring(1) + "ModuleRegistrar";
                        try {
                            Class<?> registrarClass = classLoader.loadClass(expectedClassName);
                            loadedClasses.add(registrarClass);
                        } catch (ClassNotFoundException e) {
                            System.out.println("[PLUGIN INFO] Jar mounted into runtime reference paths.");
                        }
                    }
                } catch (Exception e) {
                    diagnostics.error("DBRT099", "Failed loading jar structure: " + e.getMessage(), tenantId);
                }
            }
        }
        return loadedClasses;
    }
}

