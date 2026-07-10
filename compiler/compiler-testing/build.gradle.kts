plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {

    api(project(":compiler-sdk"))
    api(project(":compiler-core"))

    implementation("org.assertj:assertj-core:3.27.3")
    implementation("org.mockito:mockito-core:5.18.0")
    implementation("org.mockito:mockito-junit-jupiter:5.18.0")
    implementation("com.google.jimfs:jimfs:1.3.0")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
