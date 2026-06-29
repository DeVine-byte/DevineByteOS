plugins {
    java
    application
    jacoco
}

group = "org.devinebyte"
version = "0.1.0"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    }

    tasks.test {
        useJUnitPlatform()
    }
}
