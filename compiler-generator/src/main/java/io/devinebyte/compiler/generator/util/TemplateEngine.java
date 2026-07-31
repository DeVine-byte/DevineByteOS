package io.devinebyte.compiler.generator.util;

import java.util.Map;

public final class TemplateEngine {
    public static String render(String template, Map<String, String> vars) {
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return result;
    }
}
