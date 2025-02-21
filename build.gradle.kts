plugins {
    id("java")
}

group = "io.github.tavstal"
version = "1.0"

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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // PaperMC dependency
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    // HTTP Client
    compileOnly("org.apache.httpcomponents:httpclient:4.5.14")
    // HikariCP / Database Connection Pool
    compileOnly("com.zaxxer:HikariCP:4.0.3")
}

tasks.test {
    useJUnitPlatform()
}