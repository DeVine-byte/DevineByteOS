package org.devinebyte.sdk.plugin;

import org.devinebyte.sdk.service.CompilationService;
import org.devinebyte.sdk.service.ParsingService;
import org.devinebyte.sdk.service.ValidationService;

public interface PluginContext {

    ParsingService getParsingService();

    ValidationService getValidationService();

    CompilationService getCompilationService();

}
