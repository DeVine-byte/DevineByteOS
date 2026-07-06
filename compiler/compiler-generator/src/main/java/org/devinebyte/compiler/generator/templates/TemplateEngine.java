package org.devinebyte.compiler.generator.templates;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public interface TemplateEngine {

    String render(String template, Object model);

}
