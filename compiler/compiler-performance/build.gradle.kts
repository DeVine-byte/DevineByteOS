plugins {
    java
}

dependencies {
    implementation(project(":compiler-sdk"))
    testImplementation(project(":compiler-testing"))
}

tasks.test {
    useJUnitPlatform()
    systemProperty(
        "junit.jupiter.execution.parallel.enabled",
        "false"
    )
}
