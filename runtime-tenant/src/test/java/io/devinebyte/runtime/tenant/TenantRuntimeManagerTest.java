package io.devinebyte.runtime.tenant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TenantRuntimeManagerTest {
    @Test void managerCanBeConstructed() {
        assertNotNull(new TenantRuntimeManager(null, null, null));
    }
}
