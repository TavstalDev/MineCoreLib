plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

val javaVersion: String by project
val paperApiVersion: String by project
val caffeineVersion: String by project
val spiGuiVersion: String by project
val projectPackageName = "${project.group}.minecorelib"

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
    // PaperMC dependency
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:${caffeineVersion}")

    // SpiGUI for GUI creation
    implementation("com.samjakob:SpiGUI:${spiGuiVersion}")
}

// Disable the default JAR task
tasks.jar {
    enabled = false
}

// Configure the Shadow JAR task
tasks.shadowJar {
    archiveClassifier.set("") // Set the classifier for the JAR
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot" // Add custom manifest attributes
    }

    exclude("com/google/**")
    exclude("org/jspecify/**")

    // Relocate packages to avoid conflicts
    relocate("com.github.benmanes.caffeine", "${projectPackageName}.shadow.caffeine")
    relocate("com.samjakob.spigui", "${projectPackageName}.shadow.spigui")
}

// Ensure the Shadow JAR task runs during the build process
tasks.build {
    dependsOn(tasks.shadowJar)
}