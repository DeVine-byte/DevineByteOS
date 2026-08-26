package io.devinebyte.runtime.sdk.context;

import io.devinebyte.runtime.core.context.TenantContext;

public final class RuntimeContext {
    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    public static void set(TenantContext tenant) {
        CURRENT.set(tenant);
    }

    public static TenantContext current() {
        TenantContext ctx = CURRENT.get();
        if (ctx == null) {
            throw new IllegalStateException("DBRT110: No TenantContext bound to current thread");
        }
        return ctx;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
