plugins {
    `java-library`
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
    implementation(project(":compiler-parser")) // for ProgramNode
    implementation(project(":compiler-sdk"))    // for Diagnostic, Severity
}
