plugins {
    id("java-library")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {

    implementation(project(":compiler-diagnostics"))

    testImplementation(project(":compiler-testing"))

    testImplementation(platform("org.junit:junit-bom:5.10.2"))

    testImplementation("org.junit.jupiter:junit-jupiter")

}

tasks.test {
    useJUnitPlatform()
}
