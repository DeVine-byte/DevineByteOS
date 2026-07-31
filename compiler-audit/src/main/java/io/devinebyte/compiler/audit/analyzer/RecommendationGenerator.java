package io.devinebyte.compiler.audit.analyzer;

import io.devinebyte.compiler.audit.model.*;
import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RecommendationGenerator {

    public List<Recommendation> generate(
        CompilationContext context, 
        AuditModel model,
        List<GapFinding> gaps, 
        List<RiskFinding> risks
    ) {
        List<Recommendation> recs = new ArrayList<>();
        
        gaps.forEach(g -> {
            if (g.description().contains("KPI")) {
                recs.add(new Recommendation("REC_" + g.id(), "Define KPI for: " + g.description(), "KPI_ADD", g.businessUnitId()));
            } else {
                recs.add(new Recommendation("REC_" + g.id(), "Address gap: " + g.description(), "PROCESS_FIX", g.businessUnitId()));
            }
        });
        
        risks.forEach(r -> {
            if ("COMPLIANCE".equals(r.category())) {
                recs.add(new Recommendation("REC_" + r.id(), "Enable Compliance Module and add policy workflow", "MODULE_ENABLE", "COMPLIANCE"));
            } else {
                recs.add(new Recommendation("REC_" + r.id(), "Mitigate risk: " + r.description(), "RISK_MITIGATION", ""));
            }
        });
        
        context.diagnostics().addInfo("RECOMMENDATION_COMPLETE", "Generated " + recs.size() + " recommendations");
        return recs;
    }
}
