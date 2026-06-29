plugins {
    `java-library`
}

dependencies {

    api(platform("org.junit:junit-bom:5.10.2"))

    api("org.junit.jupiter:junit-jupiter")

    api("org.junit.jupiter:junit-jupiter-api")

    api("org.junit.jupiter:junit-jupiter-params")

    api("org.assertj:assertj-core:3.27.3")

    api("org.mockito:mockito-core:5.18.0")

    api("org.mockito:mockito-junit-jupiter:5.18.0")

    api("com.google.jimfs:jimfs:1.3.0")

}

tasks.test {

    useJUnitPlatform()

}
