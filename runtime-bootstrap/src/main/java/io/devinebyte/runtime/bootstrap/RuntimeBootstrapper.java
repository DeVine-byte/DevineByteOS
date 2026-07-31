package io.devinebyte.runtime.bootstrap;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.Path;
import java.util.zip.ZipFile;

@Singleton
public class RuntimeBootstrapper {
    private final DbpkgVerifier verifier;
    private final ManifestReader manifestReader;

    @Inject
    public RuntimeBootstrapper(DbpkgVerifier verifier, ManifestReader manifestReader) {
        this.verifier = verifier;
        this.manifestReader = manifestReader;
    }

    public BootstrapResult boot(TenantContext tenant, Path dbpkgPath) {
        DiagnosticCollector diagnostics = new DiagnosticCollector();

        if (!verifier.verifyStructure(tenant, dbpkgPath, diagnostics)) {
            return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics);
        }

        try (ZipFile zip = new ZipFile(dbpkgPath.toFile())) {
            var manifestEntry = zip.getEntry("manifest.json");
            var manifest = manifestReader.read(tenant, zip.getInputStream(manifestEntry), diagnostics);
            if (manifest == null || diagnostics.hasFatal()) {
                return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics);
            }

            // NEW: Strict tenant check only if multiTenant=false
            if (!manifest.multiTenant() && !manifest.tenantId().equals(tenant.tenantId())) {
                diagnostics.fatal("DBRT007", 
                    "Tenant mismatch. Expected: " + manifest.tenantId() + " Got: " + tenant.tenantId(), 
                    tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics);
            }

            if (!verifier.verifyChecksum(tenant, dbpkgPath, manifest.checksumSha256(), diagnostics)) {
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics);
            }

            return new BootstrapResult(true, tenant, manifest, dbpkgPath, diagnostics);

        } catch (Exception e) {
            diagnostics.fatal("DBRT008", "Bootstrap failed: " + e.getMessage(), tenant.tenantId());
            return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics);
        }
    }
}
