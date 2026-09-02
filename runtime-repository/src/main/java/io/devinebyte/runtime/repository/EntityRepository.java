package io.devinebyte.runtime.repository;

import java.util.Map;

public interface EntityRepository {
    String upsert(Map<String, Object> entity);
    Map<String, Object> findById(String id);
}

