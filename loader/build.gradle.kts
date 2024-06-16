plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

    implementation(project(":api"))
    implementation(project(":loader-utils"))
}

// building task operations
tasks.processResources {
    filesMatching("plugin.yml") {
        expand("pluginVersion" to project.extra["fullVersion"])
    }
}

tasks.shadowJar {
    archiveFileName = "Multiversus-Bukkit-${project.extra["fullVersion"]}.jar"

    from(project(":").tasks.shadowJar.get().archiveFile)

    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

artifacts {
    archives(tasks.shadowJar)
}