package io.devinebyte.compiler.cli.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.compiler.cli.util.CliPrinter;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.core.context.TenantLifecycle;
import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;
import io.devinebyte.compiler.sdk.CompilerOrchestrator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(
    name = "compile",
    description = "Compile .dbdsl -> tenant-vX.dbpkg",
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true
)
public class CompileCommand implements Callable<Integer> {

    @Option(names = {"-d", "--dsl"}, required = true, description = "Path to .dbdsl file")
    private Path dslFile;

    @Option(names = {"-t", "--tenant"}, required = true, description = "Tenant ID")
    private String tenantId;

    @Option(names = {"-v", "--version"}, required = true, description = "Version string e.g. 1.0.0")
    private String version;

    @Option(names = {"-o", "--output"}, description = "Output directory. Defaults to {repo-root}/execution")
    private Path outputDir;

    @Option(names = {"--strict"}, description = "Enterprise mode: 1 dbpkg = 1 tenant. Sets multiTenant: false")
    private boolean strictMode = false;

    private final CompilerOrchestrator orchestrator = new CompilerOrchestrator();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Integer call() {
        try {
            Path cwd = Path.of(System.getProperty("user.dir"));
            Path repoRoot = cwd;
            while (repoRoot != null && !Files.exists(repoRoot.resolve("settings.gradle"))) {
                repoRoot = repoRoot.getParent();
            }
            if (repoRoot == null) {
                repoRoot = Path.of("..").toAbsolutePath();
            }

            Path baseOutputDir = outputDir != null ? outputDir : repoRoot.resolve("execution");

            CliPrinter.info("Compiling: " + dslFile + " for tenant " + tenantId);
            if (strictMode) CliPrinter.info("Mode: STRICT - multiTenant: false");
            else CliPrinter.info("Mode: TEMPLATE - multiTenant: true");
            CliPrinter.info("Output Dir: " + baseOutputDir.toAbsolutePath());

            DiagnosticCollector diagnostics = new DiagnosticCollector();
            TenantContext tenant = new TenantContext(tenantId, TenantLifecycle.ACTIVE, Set.of("SALES", "INVENTORY"));
            CompilationContext context = new CompilationContext(tenant, diagnostics);

            context.put("strictMode", strictMode);

            String source = Files.readString(dslFile);

            Path aliasPath = repoRoot.resolve("tenants").resolve(tenantId).resolve("aliases.json");
            Map<String, String> keywordAliases = Files.exists(aliasPath)
                ? mapper.readValue(Files.readString(aliasPath), new TypeReference<>() {})
                : Map.of();
            context.put("keywordAliases", keywordAliases);
            context.put("sourceCode", source);
            context.put("outputDir", baseOutputDir);

            // FIXED: If your CompilerOrchestrator class supports an overloaded compile signature 
            // that accepts your initialized CompilationContext instance, pass it directly here:
            // Path dbpkg = orchestrator.compile(context, dslFile, version, baseOutputDir, strictMode);
            
            // Default Fallback: Ensure the underlying orchestrator method is looking at the correct file location
            Path dbpkg = orchestrator.compile(dslFile.toAbsolutePath(), tenantId, version, baseOutputDir, strictMode);
            CliPrinter.success("Compilation complete: " + dbpkg.toAbsolutePath());

            if (diagnostics.hasErrors()) {
                diagnostics.getDiagnostics().forEach(System.err::println);
                return 1;
            }
            return 0;
        } catch (Exception e) {
            CliPrinter.error("Compilation failed: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
