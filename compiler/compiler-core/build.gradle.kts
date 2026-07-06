plugins {
    `java-library`
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
    // Core orchestrates everything below it
    implementation(project(":compiler-sdk"))        // CompilerContext, DiagnosticSeverity, CompilerResult
    implementation(project(":compiler-parser"))     // Lexer, Parser, ProgramNode, SemanticAnalyzer
    implementation(project(":compiler-blueprint"))  // BlueprintCompiler, BlueprintModel
    implementation(project(":compiler-generator"))  // CodeGenerator, GenerationResult, SourceWriter
}
