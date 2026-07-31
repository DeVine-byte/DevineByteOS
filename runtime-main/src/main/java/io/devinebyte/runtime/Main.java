package io.devinebyte.runtime;

import io.devinebyte.runtime.bootstrap.DbpkgVerifier;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.tenant.TenantRuntime;
import io.devinebyte.runtime.tenant.TenantRuntimeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        if (args.length < 4 ||!args[0].equals("run")) {
            System.err.println("Usage: dbos run --dbpkg <path> --tenant <tenantId> [--skip-verify]");
            System.exit(1);
        }

        List<String> argList = Arrays.asList(args);
        boolean skipVerify = argList.contains("--skip-verify");

        String dbpkgArg = null;
        String tenantArg = null;

        for (int i = 0; i < args.length; i++) {
            if ("--dbpkg".equals(args[i]) && i + 1 < args.length) {
                dbpkgArg = args[i + 1];
            }
            if ("--tenant".equals(args[i]) && i + 1 < args.length) {
                tenantArg = args[i + 1];
            }
        }

        if (dbpkgArg == null || tenantArg == null) {
            System.err.println("Missing --dbpkg or --tenant");
            System.exit(1);
        }

        Path dbpkg = Path.of(dbpkgArg);

        // KEY CHANGE: pass skipVerify flag to verifier
        RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(
            new DbpkgVerifier(skipVerify),
            new ManifestReader()
        );
        ConfigurationManager configManager = new ConfigurationManager(new ObjectMapper());
        TenantRuntimeFactory factory = new TenantRuntimeFactory(configManager);

        TenantContext ctx = new TenantContext(tenantArg, TenantLifecycle.ACTIVE, Set.of());
        var bootstrap = bootstrapper.boot(ctx, dbpkg);

        if (!bootstrap.success()) {
            System.err.println("Bootstrap failed: " + bootstrap.diagnostics().getAll());
            System.exit(1);
        }

        TenantRuntime runtime = factory.create(ctx, bootstrap);
        System.out.println("✅ Booted tenant: " + runtime.tenantContext().tenantId());
        System.out.println(" From dbpkg: " + runtime.dbpkgPath());
        System.out.println(" Lifecycle: " + runtime.tenantContext().state());
    }
}

