package io.devinebyte.runtime.bootstrap;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Singleton
public class DbpkgVerifier {
    private final DbpkgStructure structure = DbpkgStructure.required();
    private final boolean skipChecksum;
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public DbpkgVerifier() {
        this(false);
    }

    public DbpkgVerifier(boolean skipChecksum) {
        this.skipChecksum = skipChecksum;
    }

    public boolean verifyStructure(TenantContext tenant, Path dbpkgPath, DiagnosticCollector diagnostics) {
        try (ZipFile zip = new ZipFile(dbpkgPath.toFile())) {
            ZipEntry manifestEntry = zip.getEntry(structure.manifestPath());
            if (manifestEntry == null) {
                diagnostics.fatal("DBRT002", "Missing required file: /manifest.json", tenant.tenantId());
                return false;
            }

            // HYBRID: Validate tenantId matches only if not multiTenant
            try (InputStream is = zip.getInputStream(manifestEntry)) {
                JsonNode manifest = mapper.readTree(is);
                String manifestTenant = manifest.path("tenantId").asText(null);
                if (manifestTenant == null) {
                    diagnostics.fatal("DBRT002", "manifest.json missing tenantId", tenant.tenantId());
                    return false;
                }

                boolean isMultiTenant = manifest.path("multiTenant").asBoolean(true); // default true

                if (!isMultiTenant &&!manifestTenant.equals(tenant.tenantId())) {
                    diagnostics.fatal("DBRT007",
                        "Tenant mismatch. Manifest: " + manifestTenant + " Requested: " + tenant.tenantId(),
                        tenant.tenantId());
                    return false;
                }
            }

            for (String dir : structure.requiredDirectories()) {
                if (zip.getEntry(dir + "/") == null) {
                    diagnostics.fatal("DBRT003", "Missing required directory: /" + dir, tenant.tenantId());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            diagnostics.fatal("DBRT004", "Failed to open.dbpkg: " + e.getMessage(), tenant.tenantId());
            return false;
        }
    }

    public boolean verifyChecksum(TenantContext tenant, Path dbpkgPath, String expectedChecksum, DiagnosticCollector diagnostics) {
        if (skipChecksum) {
            return true;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(dbpkgPath);
            String actual = bytesToHex(digest.digest(bytes));
            if (!actual.equals(expectedChecksum)) {
                diagnostics.fatal("DBRT005", "Checksum mismatch. Expected: " + expectedChecksum + " Actual: " + actual, tenant.tenantId());
                return false;
            }
            return true;
        } catch (Exception e) {
            diagnostics.fatal("DBRT006", "Checksum verification failed: " + e.getMessage(), tenant.tenantId());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
