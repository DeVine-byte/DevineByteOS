package io.devinebyte.compiler.dsl.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.dsl.ast.AstNode;
import io.devinebyte.compiler.dsl.ast.EntityNode;
import io.devinebyte.compiler.dsl.ast.ModuleNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ApiSchemaWriter {
    private final ObjectMapper mapper = new ObjectMapper();

    public static record ApiSchema(String module, List<Route> paths) {}
    public static record Route(String method, String path, String handler, String requiredPermission, String commandOrQuery) {} // ADDED

    public List<ApiSchema> generate(CompilationContext ctx, List<AstNode> ast) {
        List<ApiSchema> schemas = new ArrayList<>();

        for (AstNode node : ast) {
            if (node instanceof ModuleNode m) {
                String moduleName = m.name().toLowerCase();
                List<Route> routes = new ArrayList<>();

                for (AstNode child : m.children()) {
                    if (child instanceof EntityNode e) {
                        if (!e.exposedMethods().isEmpty()) {
                            for (String method : e.exposedMethods()) {
                                String methodUpper = method.toUpperCase();
                                String cq = methodUpper.equals("GET") ? "Query" : "Command"; // NEW
                                routes.add(new Route(
                                    methodUpper,
                                    "/" + moduleName + "/" + e.name().toLowerCase(),
                                    "Handle" + e.name() + methodUpper,
                                    moduleName.toUpperCase() + "_" + e.name().toUpperCase() + "_" + methodUpper,
                                    cq // NEW
                                ));
                            }
                        }
                    }
                }
                if (!routes.isEmpty()) {
                    schemas.add(new ApiSchema(moduleName, routes));
                }
            }
        }

        ctx.diagnostics().addInfo("API_001", "Generated " + schemas.size() + " API contracts");
        return schemas;
    }

    public void writeToFile(CompilationContext ctx, List<AstNode> ast, Path outputDir) {
        List<ApiSchema> schemas = generate(ctx, ast);
        ArrayNode root = mapper.createArrayNode();
        for (ApiSchema s : schemas) {
            ObjectNode contract = mapper.createObjectNode();
            contract.put("module", s.module());
            ArrayNode paths = mapper.createArrayNode();
            for (Route r : s.paths()) {
                ObjectNode route = mapper.createObjectNode();
                route.put("method", r.method());
                route.put("path", r.path());
                route.put("handler", r.handler());
                route.put("requiredPermission", r.requiredPermission());
                route.put("commandOrQuery", r.commandOrQuery()); // NEW
                paths.add(route);
            }
            contract.set("paths", paths);
            root.add(contract);
        }

        try {
            Path out = outputDir.resolve("contracts/APISchema.json");
            Files.createDirectories(out.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), root);
            ctx.diagnostics().addInfo("API_002", "Wrote APISchema.json with " + schemas.size() + " contracts");
        } catch (Exception ex) {
            ctx.diagnostics().addError("API_003", "Failed to write APISchema.json: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
