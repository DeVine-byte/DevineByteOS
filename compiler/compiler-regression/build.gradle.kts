plugins { java }

dependencies {
    implementation(project(":compiler-sdk"))
    testImplementation(project(":compiler-testing"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.test { useJUnitPlatform() }
