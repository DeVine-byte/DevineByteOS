package org.devinebyte.compiler.generator.java;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;
import org.devinebyte.compiler.blueprint.model.EntityDefinition;
import org.devinebyte.compiler.generator.engine.CodeGenerator;
import org.devinebyte.compiler.generator.engine.GenerationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JavaGenerator implements CodeGenerator {

    @Override
    public GenerationResult generate(BlueprintModel blueprint) {

        GenerationResult result = new GenerationResult();

        try {

            Path output =
                    Files.createTempDirectory("dbos-generated");

            for (EntityDefinition entity : blueprint.getEntities()) {

                Path file =
                        output.resolve(entity.getName() + ".java");

                StringBuilder source = new StringBuilder();

                source.append("public class ")
                      .append(entity.getName())
                      .append(" {\n");

                entity.getProperties().forEach(property -> {

                    source.append("    private ")
                          .append(property.getType())
                          .append(" ")
                          .append(property.getName())
                          .append(";\n");

                });

                source.append("}\n");

                Files.writeString(file, source.toString());

                result.add(file);
            }

            return result;

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Code generation failed.",
                    ex
            );
        }
    }
}
