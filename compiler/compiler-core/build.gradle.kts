
plugins { `java-library` }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories { mavenCentral() }

dependencies {
    api(project(":compiler-api"))
    implementation(project(":compiler-parser"))
    implementation(project(":compiler-blueprint"))
    implementation(project(":compiler-generator"))
}
