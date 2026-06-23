package compiler.core.context;

import java.util.Map;

public class CompilationContext {

    private final String tenantId;
    private final String source;
    private final Map<String, Object> metadata;

    public CompilationContext(String tenantId, String source, Map<String, Object> metadata) {
        this.tenantId = tenantId;
        this.source = source;
        this.metadata = metadata;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getSource() {
        return source;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
