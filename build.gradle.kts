plugins {
    id("java")
}

val javaVersion: String by project
val paperApiVersion: String by project
val gsonVersion: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Gson dependency
    //implementation("com.google.code.gson:gson:${gsonVersion}")

    // PaperMC dependency
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")
}

tasks.test {
    useJUnitPlatform()
}