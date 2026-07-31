package io.devinebyte.compiler.blueprint.compiler;

import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.audit.model.Recommendation;
import io.devinebyte.compiler.blueprint.model.*;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.dsl.ast.*;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ModuleCompiler {
    public BlueprintIR compile(CompilationContext context, AuditModel audit, List<AstNode> ast) {                                 
        List<ModuleIR> modules = new ArrayList<>();              
        List<EntityIR> allEntities = new ArrayList<>();          
        List<EventIR> allEvents = new ArrayList<>();             
        List<WorkflowIR> allWorkflows = new ArrayList<>();       
        List<String> kpis = new ArrayList<>();                                                                                    

        for (AstNode node : ast) {
            if (node instanceof ModuleNode m) {                  
                String moduleId = m.name().toLowerCase();
                List<EntityIR> moduleEntities = new ArrayList<>();
                List<EventIR> moduleEvents = new ArrayList<>();
                List<WorkflowIR> moduleWorkflows = new ArrayList<>();
                Set<String> deps = m.dependencies();

                for (AstNode child : m.children()) {
                    if (child instanceof EntityNode e) {
                        EntityIR eir = new EntityIR(e.name(), moduleId, e.fields(), e.name());
                        moduleEntities.add(eir);
                        allEntities.add(eir);
                    }
                    if (child instanceof EventNode ev) {
                        EventIR eir = new EventIR(ev.name(), "1.0", moduleId, ev.payload(), true);
                        moduleEvents.add(eir);
                        allEvents.add(eir);
                    }
                    if (child instanceof WorkflowNode w) {
                        // Extract requiredEvents from steps
                        Set<String> allEventNames = allEvents.stream().map(EventIR::name).collect(Collectors.toSet());
                        List<String> requiredEvents = w.steps().stream()
                            .filter(allEventNames::contains)
                            .collect(Collectors.toList());

                        WorkflowIR wir = new WorkflowIR(w.name(), moduleId, w.steps(), requiredEvents);
                        moduleWorkflows.add(wir);
                        allWorkflows.add(wir);
                    }
                    if (child instanceof KpiNode k) {
                        kpis.add(k.formula());
                    }
                }
                modules.add(new ModuleIR(moduleId, m.name(), m.enabled(), deps, moduleEntities, moduleEvents, moduleWorkflows));
            }
        }

        // Pull the enabled list early to resolve the compiler reference issue
        Set<String> tenantEnabledModules = context.tenant().enabledModules();

        audit.recommendations().stream()
            .filter(r -> "MODULE_ENABLE".equals(r.type()))
            .map(Recommendation::targetModule)
            .forEach(mod -> {
                String modId = mod.toLowerCase();
                if (modules.stream().noneMatch(m -> m.id().equals(modId))) {
                    boolean isEnabled = tenantEnabledModules.contains(mod.toUpperCase()); 
                    modules.add(new ModuleIR(
                        modId, 
                        mod, 
                        isEnabled, 
                        new HashSet<>(), 
                        new ArrayList<>(), 
                        new ArrayList<>(), 
                        new ArrayList<>()
                    ));
                }
            });

        BlueprintIR blueprint = new BlueprintIR(
            context.tenant().tenantId(),
            "1.0.0",
            tenantEnabledModules,
            modules,
            allEntities,
            allEvents,
            allWorkflows,
            kpis
        );

        // Put in context for any phase that needs it
        context.put("blueprint", blueprint);

        return blueprint;
    }
}

