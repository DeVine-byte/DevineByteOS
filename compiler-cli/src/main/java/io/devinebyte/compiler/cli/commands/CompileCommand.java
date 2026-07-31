package io.devinebyte.compiler.cli.commands;

import io.devinebyte.compiler.cli.util.CliPrinter;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.core.context.TenantLifecycle;
import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;
import io.devinebyte.compiler.dsl.ast.AstNode;
import io.devinebyte.compiler.dsl.lexer.Lexer;
import io.devinebyte.compiler.dsl.lexer.Token;
import io.devinebyte.compiler.dsl.parser.Parser;
import io.devinebyte.compiler.sdk.CompilerOrchestrator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
    private boolean strictMode = false; // NEW: HYBRID TOGGLE

    private final CompilerOrchestrator orchestrator = new CompilerOrchestrator();

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
            if(strictMode) CliPrinter.info("Mode: STRICT - multiTenant: false");
            else CliPrinter.info("Mode: TEMPLATE - multiTenant: true");
            CliPrinter.info("Output Dir: " + baseOutputDir.toAbsolutePath());

            DiagnosticCollector diagnostics = new DiagnosticCollector();
            TenantContext tenant = new TenantContext(tenantId, TenantLifecycle.ACTIVE, Set.of("SALES", "INVENTORY"));
            CompilationContext context = new CompilationContext(tenant, diagnostics);

            // NEW: Pass strictMode flag down the pipeline
            context.put("strictMode", strictMode);

            String source = Files.readString(dslFile);
            CliPrinter.info("=== FILE CONTENT ===\n" + source + "\n==================");

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens(context);

            CliPrinter.info("=== TOKEN DUMP ===");
            for (Token token : tokens) {
                System.out.printf("%-12s '%s' line:%d col:%d%n", token.type(), token.lexeme(), token.line(), token.column());
            }
            System.out.println("==================");

            Parser parser = new Parser(tokens);
            List<AstNode> ast = parser.parse(context);

            System.out.println("=== AST DUMP ===");
            System.out.println(ast);
            System.out.println("Root AST nodes count: " + (ast != null ? ast.size() : 0));
            System.out.println("================");

            context.put("ast", ast);
            context.put("outputDir", baseOutputDir); // NEW: pass outputDir so PackagingPhase uses it

            // Pass strictMode to orchestrator
            Path dbpkg = orchestrator.compile(dslFile, tenantId, version, baseOutputDir, strictMode); // UPDATED SIGNATURE
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
