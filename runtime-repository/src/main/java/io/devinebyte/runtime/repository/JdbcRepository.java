package io.devinebyte.runtime.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import java.io.File;
import java.sql.*;
import java.util.Map;

public class JdbcRepository implements EntityRepository {
    private final JdbcDataSource ds;
    private final String tableName;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public JdbcRepository(String tenantId, String moduleId, String entityName) {
        this.tableName = (moduleId + "_" + entityName).toLowerCase();

        // Dynamically point to the isolated tenant file repository
        String tenantDataPath = "./data/tenants/" + tenantId.toLowerCase();
        File tenantDir = new File(tenantDataPath);
        if (!tenantDir.exists()) {
            tenantDir.mkdirs();
        }

        this.ds = new JdbcDataSource();
        // FORCE INSTANT FLUSH: Appended WRITE_DELAY=0 to neutralize OS data-buffering delays
        this.ds.setURL("jdbc:h2:file:" + tenantDataPath + "/db_storage;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;WRITE_DELAY=0;MODE=PostgreSQL");
        this.ds.setUser("sa");
        this.ds.setPassword("");

        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "payload CLOB, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed initializing table context for: " + tableName, e);
        }
    }

    @Override
    public String upsert(Map<String, Object> entity) {
        String id = (String) entity.getOrDefault("id", java.util.UUID.randomUUID().toString());
        entity.put("id", id);
        String json = toJson(entity);
        
        // FIX: Explicitly name target destination columns to prevent column count mismatch exceptions
        String sql = "MERGE INTO " + tableName + " (id, payload) KEY(id) VALUES (?, ?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, json);
            ps.executeUpdate();
            return id;
        } catch (SQLException e) { 
            throw new RuntimeException("Database generic transactional upsert failed on " + tableName + " - SQLState: " + e.getSQLState(), e); 
        }
    }

    @Override
    public Map<String, Object> findById(String id) {
        String sql = "SELECT payload FROM " + tableName + " WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("payload");
                    return MAPPER.readValue(json, Map.class);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed processing search criteria lookup on " + tableName, e);
        }
        return null;
    }

    private String toJson(Map<String, Object> m) {
        try {
            return MAPPER.writeValueAsString(m);
        } catch (Exception e) {
            throw new RuntimeException("Context mapping serialization breakdown", e);
        }
    }
}

