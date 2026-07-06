plugins { `java-library` }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
repositories { mavenCentral() }
dependencies { 
    api(project(":compiler-api"))
    implementation(project(":compiler-blueprint"))
}
