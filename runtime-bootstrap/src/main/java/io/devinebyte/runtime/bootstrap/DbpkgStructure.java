package io.devinebyte.runtime.bootstrap;

import java.util.List;

/**
 * Contract: Every .dbpkg MUST contain these exact paths
 */
public record DbpkgStructure(
    String manifestPath,
    List<String> requiredDirectories
) {
    public static DbpkgStructure required() {
        return new DbpkgStructure(
            "manifest.json",
            List.of("contracts", "workflows", "projections", "runtime", "bootstrap")
        );
    }
}
