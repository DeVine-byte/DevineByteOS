package io.devinebyte.runtime.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @Test
    void tenantContext_requiresTenantId() {
        assertThrows(IllegalArgumentException.class, () -> 
            new TenantContext("", TenantLifecycle.ACTIVE, Set.of())
        );
    }

    @Test
    void isModuleEnabled_works() {
        TenantContext ctx = new TenantContext("acme", TenantLifecycle.ACTIVE, Set.of("sales", "inventory"));
        assertTrue(ctx.isModuleEnabled("sales"));
        assertFalse(ctx.isModuleEnabled("hr"));
    }
}
