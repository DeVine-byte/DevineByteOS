plugins { java }

dependencies {
    implementation(project(":compiler-sdk"))
    testImplementation(project(":compiler-testing"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.test {
    useJUnitPlatform()
    systemProperty ("junit.jupiter.execution.parallel.enabled", "false") // benchmarks should be serial
}
