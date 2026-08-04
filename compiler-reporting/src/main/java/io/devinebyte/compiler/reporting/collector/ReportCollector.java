package io.devinebyte.compiler.reporting.collector;

import io.devinebyte.compiler.blueprint.model.*;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.diagnostics.Diagnostic;
import io.devinebyte.compiler.core.diagnostics.DiagnosticSeverity;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.reporting.model.CompilationReport;
import io.devinebyte.compiler.reporting.model.DependencyGraph;
import io.devinebyte.compiler.reporting.model.PhaseTiming;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ReportCollector {
    private final DependencyGraphBuilder graphBuilder;
    private final List<PhaseTiming> timings = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final Map<String, Integer> moduleCounts = new HashMap<>();
    private DependencyGraph dependencyGraph = DependencyGraph.empty();
    private Instant phaseStart;

    @Inject
    public ReportCollector(DependencyGraphBuilder graphBuilder) {
        this.graphBuilder = graphBuilder;
    }

    public void startPhase(String phaseName) {
        phaseStart = Instant.now();
    }

    public void endPhase(String phaseName, boolean success) {
        timings.add(new PhaseTiming(phaseName, Duration.between(phaseStart, Instant.now()), success));
    }

    public void addWarning(String msg) { warnings.add(msg); }
    public void addError(String msg) { errors.add(msg); }
    public void addModuleCount(String module, int count) { moduleCounts.put(module, count); }

    public CompilationReport build(CompilationContext ctx, CompilerResult input, boolean success, String dbpkgPath) {
        // 1. Pull all diagnostics
        for (Diagnostic d : ctx.diagnostics().getDiagnostics()) {
            String msg = d.code() + ": " + d.message();
            if (d.severity() == DiagnosticSeverity.ERROR || d.severity() == DiagnosticSeverity.FATAL) {
                errors.add(msg);
            } else {
                warnings.add(msg);
            }
        }

        // 2. GET BLUEPRINT FROM PIPELINE OUTPUT
        BlueprintIR blueprint = (BlueprintIR) input.output();
        if (blueprint != null) {
            this.dependencyGraph = graphBuilder.build(blueprint);

            // GLOBAL TOTALS - FROM TOP LEVEL LISTS
            addModuleCount("modules", blueprint.modules().size());
            addModuleCount("entities", blueprint.entities().size());
            addModuleCount("events", blueprint.events().size());
            addModuleCount("workflows", blueprint.workflows().size());
            addModuleCount("kpis", blueprint.kpiFormulas().size());

            // PER-MODULE BREAKDOWN - GROUP BY moduleId, normalize to lowercase - FIXED
            Map<String, Long> entitiesByModule = blueprint.entities().stream()
                .collect(Collectors.groupingBy(e -> e.moduleId().toLowerCase(), Collectors.counting()));
            Map<String, Long> eventsByModule = blueprint.events().stream()
                .collect(Collectors.groupingBy(e -> e.moduleId().toLowerCase(), Collectors.counting()));
            Map<String, Long> workflowsByModule = blueprint.workflows().stream()
                .collect(Collectors.groupingBy(w -> w.moduleId().toLowerCase(), Collectors.counting()));

            blueprint.modules().forEach(m -> {
                String key = m.id().toLowerCase(); // normalize to match map keys
                int total = entitiesByModule.getOrDefault(key, 0L).intValue()
                          + eventsByModule.getOrDefault(key, 0L).intValue()
                          + workflowsByModule.getOrDefault(key, 0L).intValue();
                addModuleCount(m.name(), total); // store with pretty name
            });

            addWarning("REPORT: Found " + blueprint.workflows().size() + " workflows in final blueprint");
        } else {
            addWarning("REPORT: blueprint output is null");
        }

        return new CompilationReport(
            ctx.tenant().tenantId(),
            "1.0.0",
            success && !ctx.diagnostics().hasErrors(),
            List.copyOf(timings),
            dependencyGraph,
            Map.copyOf(moduleCounts),
            List.copyOf(warnings),
            List.copyOf(errors),
            dbpkgPath
        );
    }
}
