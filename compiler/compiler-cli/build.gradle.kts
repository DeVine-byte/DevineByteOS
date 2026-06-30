plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {

    implementation(project(":compiler-sdk"))

    testImplementation(project(":compiler-testing"))

    testImplementation(platform("org.junit:junit-bom:5.10.2"))

    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.mockito:mockito-core")

    testImplementation("org.mockito:mockito-junit-jupiter")

}
ion("org.assertj:assertj-core:3.27.3")
}

tasks.test {
    useJUnitPlatform()
}
