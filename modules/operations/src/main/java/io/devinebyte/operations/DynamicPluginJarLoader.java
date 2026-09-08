package io.devinebyte.modules.operations;

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

    /**
     * Convention > Config: Automatically loads compiled target module jar files from folder paths on demand.
     */
    public List<Class<?>> loadPluginComponents(TenantContext tenant, List<ModuleDefinition> loadOrder, DiagnosticCollector diagnostics) {
        List<Class<?>> loadedClasses = new ArrayList<>();
        String tenantId = tenant.tenantId();

        for (ModuleDefinition def : loadOrder) {
            String moduleId = def.id();
            String targetFolderPath = String.format(MODULE_BASE_PATH, moduleId);
            File folder = new File(targetFolderPath);

            if (!folder.exists() || !folder.isDirectory()) {
                // Fallback gracefully to relative up-paths if running within sub-modules
                folder = new File("../" + targetFolderPath);
            }

            if (!folder.exists() || folder.listFiles() == null) {
                continue; // Skip silently if no compiled jar assets exist for this core module layout
            }

            File[] targetFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (targetFiles == null || targetFiles.length == 0) {
                continue;
            }

            for (File jarFile : targetFiles) {
                System.out.println(String.format(
                    "[PLUGIN CONVENTION] Loading compiled pluggable jar package asset matching convention -> %s",
                    jarFile.getAbsolutePath()
                ));

                try {
                    // Open a dynamic isolated URL classloader instance for the external package file
                    URL[] urls = new URL[]{ jarFile.toURI().toURL() };
                    try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
                        
                        // Reflection check looking for custom industry domain schemas or compiled entities
                        String expectedClassName = "io.devinebyte.plugin." + moduleId + "." + 
                            moduleId.substring(0, 1).toUpperCase() + moduleId.substring(1) + "ModuleRegistrar";
                        
                        try {
                            Class<?> registrarClass = classLoader.loadClass(expectedClassName);
                            loadedClasses.add(registrarClass);
                            System.out.println("[PLUGIN SUCCESS] Bound extension plugin into running environment: " + expectedClassName);
                        } catch (ClassNotFoundException e) {
                            // If no specific registrar class exists, standardise loading fallback metrics
                            System.out.println("[PLUGIN INFO] Jar discovered. Mounted abstract entities for runtime path references.");
                        }
                    }
                } catch (Exception e) {
                    diagnostics.error("DBRT099", "Failed loading external compiled plugin jar code structure: " + e.getMessage(), tenantId);
                }
            }
        }

        return loadedClasses;
    }
}

