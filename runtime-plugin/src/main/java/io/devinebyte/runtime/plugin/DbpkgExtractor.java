package io.devinebyte.runtime.plugin;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class DbpkgExtractor {
    private DbpkgExtractor() {}

    public static void extract(ZipFile zip, Path dest, DiagnosticCollector diagnostics) throws IOException {
        Files.createDirectories(dest);
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            Path target = dest.resolve(entry.getName()).normalize();

            if (!target.startsWith(dest)) {
                // Fixed: Appended required "SYSTEM" tenantId parameter
                diagnostics.fatal("DBRT090", "Zip entry outside target dir: " + entry.getName(), "SYSTEM");
                throw new IOException("Zip slip attack");
            }

            if (entry.isDirectory()) {
                Files.createDirectories(target);
            } else {
                Files.createDirectories(target.getParent());
                try (InputStream is = zip.getInputStream(entry)) {
                    Files.copy(is, target);
                }
            }
        }
    }
}

