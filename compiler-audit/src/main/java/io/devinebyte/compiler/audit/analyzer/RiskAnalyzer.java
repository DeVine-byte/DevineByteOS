package io.devinebyte.compiler.audit.analyzer;

import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.audit.model.RiskFinding;
import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RiskAnalyzer {

    public List<RiskFinding> analyze(CompilationContext context, AuditModel model) {
        List<RiskFinding> risks = new ArrayList<>();
        
        boolean hasFinance = model.businessUnits().stream()
            .flatMap(bu -> bu.modules().stream())
            .anyMatch(m -> "FINANCE".equalsIgnoreCase(m));
            
        boolean hasSOD = model.processes().stream()
            .anyMatch(p -> p.name().toLowerCase().contains("segregation"));
            
        if (hasFinance && !hasSOD) {
            risks.add(new RiskFinding("RISK_SOD", "Finance enabled without Segregation of Duties", "COMPLIANCE", 5));
            context.diagnostics().addWarning("RISK_001", "Finance enabled without SoD process");
        }
        
        if (model.kpis().isEmpty()) {
            risks.add(new RiskFinding("RISK_NO_KPI", "No KPIs defined for business", "OPERATIONAL", 4));
        }
        
        context.diagnostics().addInfo("RISK_ANALYSIS_COMPLETE", "Found " + risks.size() + " risks");
        return risks;
    }
}
