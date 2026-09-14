plugins {
    `java-library`
    alias(libs.plugins.run.paper)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.runServer {
    minecraftVersion(libs.versions.minecraft.get())
}

tasks.processResources {
    val props = mapOf("version" to version)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
