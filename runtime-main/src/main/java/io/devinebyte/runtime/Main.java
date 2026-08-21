package io.devinebyte.runtime;

import io.devinebyte.runtime.tenant.RuntimeLauncher;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 4 || !args[0].equals("run")) {
            System.err.println("Usage: dbos run --dbpkg <path> --tenant <tenantId> [--skip-verify]");
            System.exit(1);
        }

        boolean skipVerify = Arrays.asList(args).contains("--skip-verify");
        String dbpkgArg = null, tenantArg = null;
        for (int i = 0; i < args.length; i++) {
            if ("--dbpkg".equals(args[i]) && i + 1 < args.length) dbpkgArg = args[i + 1];
            if ("--tenant".equals(args[i]) && i + 1 < args.length) tenantArg = args[i + 1];
        }
        if (dbpkgArg == null || tenantArg == null) {
            System.err.println("Missing required --dbpkg or --tenant routing configurations");
            System.exit(1);
        }

        // Invoke the core launcher
        RuntimeLauncher.launch(Path.of(dbpkgArg), tenantArg, skipVerify);
    }
}
