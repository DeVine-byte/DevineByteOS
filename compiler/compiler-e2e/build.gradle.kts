plugins {
    java
}

dependencies {
    implementation(project(":compiler-sdk"))

    testImplementation(project(":compiler-testing"))

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Required by Gradle 9.x
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
