plugins {
    `java-library`
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
    implementation(project(":compiler-blueprint")) // for BlueprintModel
    implementation(project(":compiler-sdk"))       // if you use SDK types
}
