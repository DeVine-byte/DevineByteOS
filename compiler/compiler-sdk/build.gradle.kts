plugins {
    id("java-library")
}

dependencies {

    api(project(":compiler-api"))

    implementation(project(":compiler-blueprint"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}
