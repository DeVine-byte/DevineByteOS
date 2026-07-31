package io.devinebyte.compiler.sdk.api;

import io.devinebyte.compiler.core.context.TenantContext;
import java.nio.file.Path;

public record CompilationRequest(
    TenantContext tenant,
    Path blueprintPath,
    String version
) {}
