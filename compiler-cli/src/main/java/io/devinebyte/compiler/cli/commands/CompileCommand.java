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
import java.util.regex.Pattern;

@Command(
    name = "compile",
    description = "Compile .dbdsl -> tenant-vX.dbpkg using enterprise constraints",
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true
)
public class CompileCommand implements Callable<Integer> {

    // Simple SemVer regex rule matching MAJOR.MINOR.PATCH format
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.]+)?$");

    @Option(names = {"-d", "--dsl"}, required = true, description = "Path to the target .dbdsl schema file")
    private Path dslFile;

    @Option(names = {"-t", "--tenant"}, required = true, description = "Unique corporate Tenant ID string")
    private String tenantId;

    @Option(names = {"-v", "--version"}, required = true, description = "Semantic version format (e.g., 1.0.0)")
    private String version;

    @Option(names = {"-o", "--out", "--output"}, description = "Target output container binary location directory")
    private Path outputDir;

    @Option(names = {"--strict"}, description = "Enterprise Single-Tenant isolation rule. Forces multiTenant=false")
    private boolean strictMode = false;

    @Option(names = {"--multi-tenant"}, description = "SaaS template distribution architecture rule. Sets multiTenant=true")
    private boolean multiTenantMode = true;

    private final CompilerOrchestrator orchestrator = new CompilerOrchestrator();
    private final ObjectMapper mapper = new ObjectMapper();

    @Option(names = {"--debug"}, description = "Verbose Mode: Prints structural syntax tokens and full execution JSON reports")
    private boolean debugMode = false;

    @Override
    public Integer call() {
        // 1. Strict Validation Rule: Enforce Semantic Versioning Compliance Constraints
        if (!SEMVER_PATTERN.matcher(version).matches()) {
            CliPrinter.error("CLI ARGUMENT FAULT: Provided version string [" + version + "] fails SemVer compliance checking rules.");
            CliPrinter.info("Remediation: Rectify parameter layout to match structural 'MAJOR.MINOR.PATCH' formatting keys.");
            return 1;
        }

        // 2. Strict Flag Rule: If --strict is true, multi-tenant distribution behavior is immediately disengaged
        boolean computeMultiTenantResult = !strictMode;
        if (strictMode) {
            this.multiTenantMode = false;
        }

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

            CliPrinter.info("Initializing DevineByte Compilation Sequence...");
            CliPrinter.info("  ├─► Target DSL: " + dslFile.toAbsolutePath());
            CliPrinter.info("  ├─► Client ID:  " + tenantId);
            CliPrinter.info("  ├─► Version:    " + version);
            CliPrinter.info("  └─► Strictness: SINGLE_TENANT=" + strictMode + " (multiTenant=" + computeMultiTenantResult + ")");

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

            // Pass execution metrics downstream to package builders
            Path dbpkg = orchestrator.compile(dslFile.toAbsolutePath(), tenantId, version, baseOutputDir, strictMode);
            CliPrinter.success("Enterprise platform package artifact generated seamlessly at: " + dbpkg.toAbsolutePath());

            if (diagnostics.hasErrors()) {
                diagnostics.getDiagnostics().forEach(System.err::println);
                return 1;
            }
            return 0;
        } catch (Exception e) {
            CliPrinter.error("Substrate compilation crashed out: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}

