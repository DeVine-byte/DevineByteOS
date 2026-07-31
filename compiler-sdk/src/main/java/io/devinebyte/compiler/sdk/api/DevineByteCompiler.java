package io.devinebyte.compiler.sdk.api;

import jakarta.inject.Singleton;

@Singleton
public interface DevineByteCompiler {
    CompilationResult compile(CompilationRequest request);
}
