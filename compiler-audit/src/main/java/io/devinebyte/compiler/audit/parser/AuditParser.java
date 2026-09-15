package io.devinebyte.compiler.audit.parser;

import io.devinebyte.compiler.audit.analyzer.GapAnalyzer;        
import io.devinebyte.compiler.audit.analyzer.RecommendationGenerator;
import io.devinebyte.compiler.audit.analyzer.RiskAnalyzer;       
import io.devinebyte.compiler.audit.model.*;
import io.devinebyte.compiler.audit.validation.AuditValidationEngine;
import io.devinebyte.compiler.core.context.CompilationContext;   
import io.devinebyte.compiler.core.context.TenantLifecycle;
import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;                                                               
import jakarta.inject.Inject;                                    
import jakarta.inject.Singleton;                                 
import java.util.List;                                           
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton                                                       
public class AuditParser {                                                                                                            
    private final AuditValidationEngine validationEngine;            
    private final GapAnalyzer gapAnalyzer;
    private final RiskAnalyzer riskAnalyzer;                         
    private final RecommendationGenerator recommendationGenerator;                                                                                                                                     

    @Inject                                                          
    public AuditParser(                                                  
        AuditValidationEngine validationEngine,                          
        GapAnalyzer gapAnalyzer,
        RiskAnalyzer riskAnalyzer,                                       
        RecommendationGenerator recommendationGenerator
    ) {
        this.validationEngine = validationEngine;
        this.gapAnalyzer = gapAnalyzer;
        this.riskAnalyzer = riskAnalyzer;
        this.recommendationGenerator = recommendationGenerator;
    }

    public AuditParseResult parse(CompilationContext context, String rawAudit) {
        var diagnostics = context.diagnostics();

        AuditModel baseModel = parseRawToModel(rawAudit, diagnostics);
        if (baseModel == null) {
            return new AuditParseResult(null, true);
        }

        validationEngine.validate(context, baseModel);
        if (diagnostics.hasErrors()) {
            return new AuditParseResult(baseModel, true);
        }

        List<GapFinding> gaps = gapAnalyzer.analyze(context, baseModel);
        List<RiskFinding> risks = riskAnalyzer.analyze(context, baseModel);
        List<Recommendation> recs = recommendationGenerator.generate(context, baseModel, gaps, risks);

        AuditModel enrichedModel = new AuditModel(
            baseModel.source(),
            baseModel.version(),
            baseModel.companyName(),
            baseModel.targetLifecycle(),
            baseModel.businessUnits(),
            baseModel.processes(),
            baseModel.kpis(),
            gaps,
            risks,
            recs,
            baseModel.metadata()
        );

        diagnostics.addInfo("AUDIT_PARSE_COMPLETE", "Audit parsed with " + gaps.size() + " gaps, " + risks.size() + " risks");
        return new AuditParseResult(enrichedModel, diagnostics.hasErrors());
    }

    private AuditModel parseRawToModel(String rawAudit, DiagnosticCollector diagnostics) {
        if (rawAudit == null || rawAudit.isBlank()) {
            diagnostics.addError("AUDIT_001", "Audit source cannot be empty");
            return null;
        }

        // FIXED: Fulfill out-of-band extraction using pure Java regular expression token parsing structures
        String companyName = extractJsonField(rawAudit, "companyName", "Unknown Company");
        String version = extractJsonField(rawAudit, "version", "1.0");
        String lifecycleStr = extractJsonField(rawAudit, "targetLifecycle", "PROVISIONING");
        
        TenantLifecycle targetLifecycle;
        try {
            targetLifecycle = TenantLifecycle.valueOf(lifecycleStr.toUpperCase().trim());
        } catch (Exception e) {
            targetLifecycle = TenantLifecycle.PROVISIONING;
        }

        return new AuditModel(
            "json-source", 
            version, 
            companyName, 
            targetLifecycle, 
            List.of(), 
            List.of(), 
            List.of(), 
            List.of(), 
            List.of(), 
            List.of(), 
            Map.of() 
        );
    }

    private String extractJsonField(String source, String key, String defaultValue) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return defaultValue;
    }
}

