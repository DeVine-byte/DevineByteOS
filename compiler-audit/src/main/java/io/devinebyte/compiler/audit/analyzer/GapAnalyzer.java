package io.devinebyte.compiler.audit.analyzer;

import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.audit.model.GapFinding;
import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GapAnalyzer {

    public List<GapFinding> analyze(CompilationContext context, AuditModel model) {
        List<GapFinding> gaps = new ArrayList<>();
        
        model.processes().forEach(p -> {
            boolean hasKpi = model.kpis().stream().anyMatch(k -> k.id().startsWith(p.id()));
            if (!hasKpi) {
                gaps.add(new GapFinding(
                    "GAP_" + p.id(),
                    "Process " + p.name() + " has no KPI defined",
                    "MEDIUM",
                    p.businessUnitId()
                ));
            }
        });
        
        model.businessUnits().forEach(bu -> {
            if (bu.modules().isEmpty()) {
                gaps.add(new GapFinding(
                    "GAP_" + bu.id(),
                    "Business Unit " + bu.name() + " has no modules enabled",
                    "HIGH",
                    bu.id()
                ));
            }
        });
        
        context.diagnostics().addInfo("GAP_ANALYSIS_COMPLETE", "Found " + gaps.size() + " gaps");
        return gaps;
    }
}
