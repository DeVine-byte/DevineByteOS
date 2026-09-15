package io.devinebyte.runtime.bootstrap;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class DbpkgVerifier {
    private final DbpkgStructure structure = DbpkgStructure.required();
    private final ObjectMapper mapper = new ObjectMapper();

    // Pure Java structure initialization constructor
    public DbpkgVerifier() {}

    public boolean verifyStructure(TenantContext tenant, Path dbpkgPath, DiagnosticCollector diagnostics) {
        try (ZipFile zip = new ZipFile(dbpkgPath.toFile())) {
            ZipEntry manifestEntry = zip.getEntry(structure.manifestPath());
            if (manifestEntry == null) {
                diagnostics.fatal("DBRT002", "Security Failure: Missing critical tracking file /manifest.json", tenant.tenantId());
                return false;
            }

            try (InputStream is = zip.getInputStream(manifestEntry)) {
                JsonNode manifest = mapper.readTree(is);
                String manifestTenant = manifest.path("tenantId").asText(null);
                String embeddedSha = manifest.path("sha256").asText(null);

                if (manifestTenant == null) {
                    diagnostics.fatal("DBRT002", "Security Failure: manifest.json structure is missing tenantId reference", tenant.tenantId());
                    return false;
                }

                if (embeddedSha == null || embeddedSha.isBlank()) {
                    diagnostics.fatal("DBRT002", "Security Failure: manifest.json structure is missing required sha256 checksum tracking attribute", tenant.tenantId());
                    return false;
                }

                if (!verifyChecksum(tenant, dbpkgPath, embeddedSha, diagnostics)) {
                    return false;
                }

                boolean isMultiTenant = manifest.path("multiTenant").asBoolean(true);
                if (!isMultiTenant && !manifestTenant.equals(tenant.tenantId())) {
                    diagnostics.fatal("DBRT007",
                        "Tenant Mismatch Gating: Package restricted exclusively to client " + manifestTenant + ", but requested by " + tenant.tenantId(),
                        tenant.tenantId());
                    return false;
                }
            }

            // FIXED: Verify partition presence by scanning file path prefixes, preventing empty metadata folder failures
            for (String requiredDir : structure.requiredDirectories()) {
                String targetPrefix = requiredDir + "/";
                boolean prefixDiscovered = zip.stream()
                    .anyMatch(entry -> entry.getName().startsWith(targetPrefix));

                if (!prefixDiscovered) {
                    diagnostics.fatal("DBRT003", "Missing required structure partition directory: /" + requiredDir, tenant.tenantId());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            diagnostics.fatal("DBRT004", "Security Failure: Target package structure manipulation or failure detected: " + e.getMessage(), tenant.tenantId());
            return false;
        }
    }

    public boolean verifyChecksum(TenantContext tenant, Path dbpkgPath, String expectedChecksum, DiagnosticCollector diagnostics) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            try (ZipFile zip = new ZipFile(dbpkgPath.toFile())) {
                java.util.List<? extends ZipEntry> sortedEntries = java.util.Collections.list(zip.entries());
                sortedEntries.sort(java.util.Comparator.comparing(ZipEntry::getName));
                
                for (ZipEntry entry : sortedEntries) {
                    if (entry.isDirectory() || "manifest.json".equals(entry.getName())) {
                        continue;
                    }
                    
                    try (InputStream is = zip.getInputStream(entry)) {
                        byte[] buffer = new byte[8192];
                        int readBytes;
                        while ((readBytes = is.read(buffer)) != -1) {
                            digest.update(buffer, 0, readBytes);
                        }
                    }
                }
            }

            String actual = bytesToHex(digest.digest());
            if (!actual.equalsIgnoreCase(expectedChecksum)) {
                diagnostics.fatal("DBRT005", "Security Failure: Package binary footprint modified (SHA-256 Checksum Mismatch). Expected: " + expectedChecksum + " Actual: " + actual, tenant.tenantId());
                return false;
            }
            return true;
        } catch (Exception e) {
            diagnostics.fatal("DBRT006", "Security Failure: Critical error calculating stream validation tracking signature: " + e.getMessage(), tenant.tenantId());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

