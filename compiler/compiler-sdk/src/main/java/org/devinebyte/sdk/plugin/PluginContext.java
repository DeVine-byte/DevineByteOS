package org.devinebyte.sdk.plugin;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import org.devinebyte.compiler.api.CompilationService;
import org.devinebyte.compiler.api.ParsingService;
import org.devinebyte.compiler.api.ValidationService;

public interface PluginContext {

    ParsingService getParsingService();

    ValidationService getValidationService();

    CompilationService getCompilationService();

}
