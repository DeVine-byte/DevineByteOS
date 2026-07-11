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

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}
application {
    mainClass.set("org.devinebyte.compiler.cli.CliApplication")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {

    manifest {
        attributes(
            "Main-Class" to "org.devinebyte.compiler.cli.CliApplication"
        )
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({
        configurations.runtimeClasspath.get()
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
    })
}
