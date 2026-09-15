package io.devinebyte.runtime;

import io.devinebyte.runtime.tenant.RuntimeLauncher;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 5 || !"run".equals(args[0])) {
            System.err.println("Usage Error: dbos run --dbpkg <path> --tenant <tenantId>");
            System.exit(1);
        }

        String dbpkgArg = null;
        String tenantArg = null;

        for (int i = 1; i < args.length; i++) {
            if ("--dbpkg".equals(args[i]) && i + 1 < args.length) {
                dbpkgArg = args[i + 1];
            }
            if ("--tenant".equals(args[i]) && i + 1 < args.length) {
                tenantArg = args[i + 1];
            }
        }

        if (dbpkgArg == null || tenantArg == null) {
            System.err.println("Fatal Ingress Error: Missing required --dbpkg or --tenant routing configurations.");
            System.exit(1);
        }

        // Secure Launch: Invoke the launcher engine with validation active
        RuntimeLauncher.launch(Path.of(dbpkgArg), tenantArg);
    }
}

