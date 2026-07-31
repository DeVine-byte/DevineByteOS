package io.devinebyte.compiler.reporting.writer;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.reporting.model.CompilationReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class JsonReportWriter {
    private final ObjectMapper mapper;

    public JsonReportWriter() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // CHANGED: write to outputDir/{tenant}/ instead of hardcoded "execution"
    public Path write(CompilationContext ctx, CompilationReport report) throws Exception {
        // FIX: Use outputDir from context
        Path baseOutputDir = ctx.get("outputDir");
        if (baseOutputDir == null) {
            baseOutputDir = Path.of("execution");
        }
        Path reportDir = baseOutputDir.resolve(ctx.tenant().tenantId());
        Files.createDirectories(reportDir);

        Path out = reportDir.resolve("tenant-" + ctx.tenant().tenantId() + "-report.json");
        String json = mapper.writeValueAsString(report);
        System.out.println("[DEBUG REPORT JSON] " + json);
        Files.writeString(out, json);

        System.out.println("[REPORT] Wrote report to " + out.toAbsolutePath());
        return out;
    }
}
