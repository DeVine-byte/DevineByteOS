plugins {
    `java`
    application
}

group = "org.devinebyte"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":compiler-sdk"))
    implementation(project(":compiler-core"))
    implementation(project(":compiler-blueprint"))
    implementation(project(":compiler-api"))

    implementation("org.assertj:assertj-core:3.27.3")

    testImplementation(project(":compiler-testing"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

application {
    mainClass.set("org.devinebyte.compiler.cli.CliApplication")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.devinebyte.compiler.cli.CliApplication"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
