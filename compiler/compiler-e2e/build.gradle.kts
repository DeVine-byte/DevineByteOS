plugins {
    java
}

dependencies {
    testImplementation(project(":compiler-testing"))
    implementation(project(":compiler-sdk"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
}
