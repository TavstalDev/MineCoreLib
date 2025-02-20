plugins {
    id("java")
}

group = "io.github.tavstal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // PaperMC dependency
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    // Vault API
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    // Placeholder API
    compileOnly("me.clip:placeholderapi:2.11.6")
    // SpiGUI API
    compileOnly("com.samjakob:SpiGUI:1.3.1")
    // HTTP Client
    compileOnly("org.apache.httpcomponents:httpclient:4.5.14")
    // HikariCP / Database Connection Pool
    compileOnly("com.zaxxer:HikariCP:4.0.3")
}

tasks.test {
    useJUnitPlatform()
}