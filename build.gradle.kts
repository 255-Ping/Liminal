plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    jar {
        destinationDirectory.set(file("/home/ping/Documents/LiminalServer/plugins"))
    }

    processResources {
        val props = mapOf("version" to version)
        // Without an explicit input, Gradle's configuration cache treats processResources as
        // up-to-date across version bumps and the expanded plugin.yml stays stale. Declare
        // version as an input so the task re-runs whenever it changes.
        inputs.property("version", version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
