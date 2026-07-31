package io.devinebyte.compiler.projection.compiler;

import jakarta.inject.Singleton;

@Singleton
public class WasmGenerator {
    // Stub: Real implementation would use GraalVM or TeaVM to compile fold() to WASM
    // For now we emit a placeholder to satisfy .dbpkg contract
    public String generateWasm(String eventType, String projectionName) {
        return "PLACEHOLDER_WASM_BASE64_FOR_" + projectionName + "_" + eventType;
    }
}
