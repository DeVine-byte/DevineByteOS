package org.devinebyte.compiler.api.plugin;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.service.CompilationService;
import org.devinebyte.compiler.api.service.ParsingService;
import org.devinebyte.compiler.api.service.ValidationService;

public interface PluginContext {

    ParsingService getParsingService();

    ValidationService getValidationService();

    CompilationService getCompilationService();

}
