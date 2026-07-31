package io.devinebyte.compiler.reporting.phase;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.reporting.collector.ReportCollector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ReportingPhase implements CompilerPhase {

    private final ReportCollector collector;

    @Inject
    public ReportingPhase(ReportCollector collector) {
        this.collector = collector;
    }

    @Override
    public String name() {
        return "reporting";
    }

    @Override
    public CompilerResult execute(CompilationContext ctx, CompilerResult input) {
        // Do nothing here. Orchestrator will call collector.build() after all phases
        return input;
    }
}
