package io.devinebyte.compiler.cli.commands;

import io.devinebyte.compiler.audit.parser.AuditParser;
import io.devinebyte.compiler.audit.parser.AuditParseResult;
import io.devinebyte.compiler.audit.analyzer.GapAnalyzer;
import io.devinebyte.compiler.audit.analyzer.RiskAnalyzer;
import io.devinebyte.compiler.audit.analyzer.RecommendationGenerator;
import io.devinebyte.compiler.audit.validation.AuditValidationEngine;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.core.context.TenantLifecycle;
import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;
import io.devinebyte.compiler.cli.util.CliPrinter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "audit", description = "Parse business audit and generate gaps/risks")
public class AuditCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to audit.json")
    private Path auditPath;

    @Override
    public Integer call() throws Exception {
        CliPrinter.info("Parsing audit: " + auditPath);

        String json = Files.readString(auditPath);
        TenantContext tenant = new TenantContext("cli", TenantLifecycle.ACTIVE, Set.of());
        DiagnosticCollector diagnostics = new DiagnosticCollector();
        CompilationContext ctx = new CompilationContext(tenant, diagnostics);

        // NEW: instantiate manually
        AuditParser parser = new AuditParser(
            new AuditValidationEngine(),
            new GapAnalyzer(),
            new RiskAnalyzer(),
            new RecommendationGenerator()
        );
        
        AuditParseResult result = parser.parse(ctx, json);

        if (diagnostics.hasErrors()) {
            CliPrinter.error("Audit parsing failed");
            diagnostics.getDiagnostics().forEach(d -> CliPrinter.diagnostic(d));
            return 1;
        }

        CliPrinter.success("Audit parsed. Processes: " + result.model().processes().size());
        CliPrinter.info("Gaps: " + result.model().gaps().size());
        CliPrinter.info("Risks: " + result.model().risks().size());
        return 0;
    }
}
