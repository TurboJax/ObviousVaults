plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.turbojax.org/releases")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(libs.turbo.messages)
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
