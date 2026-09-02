package io.devinebyte.runtime.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryFactory {
    private static final Map<String, EntityRepository> cache = new ConcurrentHashMap<>();

    public static EntityRepository get(String tenantId, String moduleId, String entityName) {
        // Multi-tenant isolation calculation key string composition
        String key = (tenantId + ":" + moduleId + ":" + entityName).toLowerCase();
        return cache.computeIfAbsent(key, k -> new JdbcRepository(tenantId, moduleId, entityName));
    }
}

