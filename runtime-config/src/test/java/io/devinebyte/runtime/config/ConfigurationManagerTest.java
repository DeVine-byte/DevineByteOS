package io.devinebyte.runtime.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext; // FIXED PACKAGE
import io.devinebyte.runtime.core.context.TenantLifecycle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationManagerTest {
    @Test 
    void loadsModuleGraph(@TempDir Path temp) throws Exception {
        // New format: {"modules": {"SALES": {...}, "INVENTORY": {...}}}
        String json = """
        {
          "modules": {
            "SALES": {"moduleId":"SALES","enabled":true,"dependsOn":[],"exposesEvents":[],"subscribesToEvents":[]},
            "INVENTORY": {"moduleId":"INVENTORY","enabled":false,"dependsOn":[],"exposesEvents":[],"subscribesToEvents":[]}
          }
        }
        """;
        Files.writeString(temp.resolve("module_graph.json"), json);
        
        var mgr = new ConfigurationManager(new ObjectMapper()); // inject mapper
        var graph = mgr.loadModuleGraph(new TenantContext("acme", TenantLifecycle.ACTIVE, Set.of()), temp);
        
        assertTrue(graph.isEnabled("SALES"));
        assertFalse(graph.isEnabled("INVENTORY"));
    }
}
