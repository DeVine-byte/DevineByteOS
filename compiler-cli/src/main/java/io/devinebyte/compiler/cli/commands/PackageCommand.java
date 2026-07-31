package io.devinebyte.compiler.cli.commands;

import io.devinebyte.compiler.sdk.CompilerOrchestrator;
import io.devinebyte.compiler.cli.util.CliPrinter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "package", description = "Build tenant-vX.dbpkg from DSL using full pipeline")
public class PackageCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, required = true) private Path dslFile; // was missing
    @Option(names = {"-t", "--tenant"}, required = true) private String tenantId;
    @Option(names = {"-v", "--version"}, required = true) private String version;
    @Option(names = {"-o", "--output"}, defaultValue = "./execution") private Path outputDir; // CHANGED: default to execution

    @Override
    public Integer call() {
        try {
            CliPrinter.info("Compiling + Packaging tenant: " + tenantId + " v" + version);

            CompilerOrchestrator orchestrator = new CompilerOrchestrator();
            Path dbpkg = orchestrator.compile(dslFile, tenantId, version, outputDir); // SINGLE CALL

            CliPrinter.success("Package built: " + dbpkg.toAbsolutePath());
            return 0;
        } catch (Exception e) {
            CliPrinter.error("Packaging failed: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
