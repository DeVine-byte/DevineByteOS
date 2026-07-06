plugins {
    `java-library`
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
    api(project(":compiler-core")) // for CompilerEngine, CompilerConfiguration
    implementation(project(":compiler-cli")) // for CliOptions
}
