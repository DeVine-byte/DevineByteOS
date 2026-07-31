package io.devinebyte.compiler.contracts.phase;
import com.fasterxml.jackson.databind.ObjectMapper; import com.fasterxml.jackson.databind.SerializationFeature;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.contracts.generator.*;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase; import io.devinebyte.compiler.core.pipeline.CompilerResult;
import jakarta.inject.Inject; import jakarta.inject.Singleton; import java.nio.file.Files; import java.nio.file.Path;

@Singleton
public class ContractsPhase implements CompilerPhase {
    private final EventSchemaGenerator eventGen; private final EntitySchemaGenerator entityGen;
    private final WorkflowSchemaGenerator workflowGen; private final APISchemaGenerator apiGen;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Inject public ContractsPhase(EventSchemaGenerator eventGen, EntitySchemaGenerator entityGen, WorkflowSchemaGenerator workflowGen, APISchemaGenerator apiGen) {
        this.eventGen = eventGen; this.entityGen = entityGen; this.workflowGen = workflowGen; this.apiGen = apiGen;
    }

    @Override public String name() { return "contracts"; }

    @Override
    public CompilerResult execute(CompilationContext context, CompilerResult input) {
        BlueprintIR ir = (BlueprintIR) input.output();
        context.diagnostics().addInfo("CONTRACTS", "Generating 4 contract schemas for tenant " + context.tenant().tenantId());
        var events = eventGen.generate(context.tenant(), ir);
        var entities = entityGen.generate(context.tenant(), ir);
        var workflows = workflowGen.generate(context.tenant(), ir);
        var apis = apiGen.generate(context.tenant(), ir);
        try {
            Path outDir = Path.of("build/contracts"); Files.createDirectories(outDir);
            mapper.writeValue(outDir.resolve("EventSchema.json").toFile(), events);
            mapper.writeValue(outDir.resolve("EntitySchema.json").toFile(), entities);
            mapper.writeValue(outDir.resolve("WorkflowSchema.json").toFile(), workflows);
            mapper.writeValue(outDir.resolve("APISchema.json").toFile(), apis);
        } catch (Exception e) { context.diagnostics().addError("CONTRACTS_IO", "Failed to write contracts: " + e.getMessage()); }
        return new CompilerResult<>(context.tenant(), context.diagnostics(), ir); // pass-through
    }
}
