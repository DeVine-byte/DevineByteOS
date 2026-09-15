package io.devinebyte.compiler.generator.phase;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EventIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.generator.codegen.DomainGenerator;
import io.devinebyte.compiler.generator.model.GenerationResult;
import io.devinebyte.compiler.generator.runtime.RuntimeBootstrapGenerator;
import io.devinebyte.compiler.generator.runtime.RuntimeConfigGenerator;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class GeneratorPhase implements CompilerPhase {
    private final DomainGenerator domainGenerator;
    private final RuntimeConfigGenerator configGenerator;
    private final RuntimeBootstrapGenerator bootstrapGenerator;

    @Inject
    public GeneratorPhase(DomainGenerator domainGenerator, RuntimeConfigGenerator configGenerator, RuntimeBootstrapGenerator bootstrapGenerator) {
        this.domainGenerator = domainGenerator;
        this.configGenerator = configGenerator;
        this.bootstrapGenerator = bootstrapGenerator;
    }

    @Override
    public String name() {
        return "generator";
    }

    @Override
    public CompilerResult<GenerationResult> execute(CompilationContext context, CompilerResult input) {

        BlueprintIR ir = (BlueprintIR) input.output();
        try {
            // FIXED: Fulfill path extraction via key-value data maps to prevent signature compilation errors
            String customOutPath = (String) context.get("out");
            Path base;

            if (customOutPath != null && !customOutPath.isBlank()) {
                base = Path.of(customOutPath);
            } else {
                // FIXED: Enforce cross-platform sandbox isolation by extracting the JVM's dynamic temp path property
                String systemTmp = System.getProperty("java.io.tmpdir", "/tmp");
                String tenantId = context.tenant().tenantId();
                String version = (String) context.get("version");
                if (version == null || version.isBlank()) {
                    version = "1.0.0";
                }
                base = Path.of(systemTmp).resolve("devinebyte-build").resolve(tenantId).resolve(version);
            }

            Files.createDirectories(base);

            // 1. Generate artifacts inside the properly isolated build directory partition
            GenerationResult domainResult = domainGenerator.generate(context.tenant(), ir, base);
            GenerationResult configResult = configGenerator.generate(context.tenant(), ir, base.resolve("runtime"));
            byte[] bootstrap = bootstrapGenerator.generate(context.tenant(), base.resolve("bootstrap"));

            // 2. Build ModuleGraph with UPPERCASE ids so deps match keys
            Map<String, ModuleDefinition> modules = ir.modules().stream()
                .collect(Collectors.toMap(
                    m -> m.name().toUpperCase(),
                    m -> new ModuleDefinition(
                        m.name().toUpperCase(),
                        context.tenant().enabledModules().contains(m.name().toUpperCase()),
                        m.dependencies().stream().map(String::toUpperCase).collect(Collectors.toSet()),
                        m.events().stream().map(EventIR::name).collect(Collectors.toSet()),
                        Set.of()
                    )
                ));
            ModuleGraph moduleGraph = new ModuleGraph(modules);

            // 3. Put everything in context for PackagingPhase
            context.put("eventSchemas", domainResult.eventSchemas());
            context.put("entitySchemas", domainResult.entitySchemas());
            context.put("workflowSchemas", domainResult.workflowSchemas());
            context.put("apiSchemas", domainResult.apiSchemas());
            context.put("workflows", domainResult.workflows());
            context.put("projections", configResult.projections());
            context.put("dashboards", configResult.dashboards());
            context.put("runtimeBootstrap", bootstrap);
            context.put("tenantConfig", configResult.config());
            context.put("featureFlags", configResult.featureFlags());
            context.put("moduleGraph", moduleGraph);

            context.diagnostics().addInfo("GENERATOR", "Generated domain, runtime, bootstrap for tenant " + context.tenant().tenantId() + " at target: " + base.toAbsolutePath());
            return new CompilerResult<>(context.tenant(), context.diagnostics(), configResult);

        } catch (Exception e) {
            context.diagnostics().addError("GENERATOR_IO", e.getMessage());
            return new CompilerResult<>(context.tenant(), context.diagnostics(), null);
        }
    }
}

