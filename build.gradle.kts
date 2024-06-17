plugins {
    id("java")
    id("java-library")
    alias(libs.plugins.shadow)
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    // project settings
    group = "me.kubbidev"
    version = "1.0-SNAPSHOT"

    base {
        archivesName.set("multiversus")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // include source in when publishing
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    // make fullVersion accessible in subprojects
    project.extra["fullVersion"] = "1.0.2"
    project.extra["apiVersion"] = "1.0"
}

dependencies {
    api(project(":api"))
    compileOnly(project(":loader-utils"))

    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("me.lucko:commodore:2.0")

    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("org.slf4j:slf4j-api:1.7.30")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.0")

    api("net.kyori:adventure-api:4.16.0") {
        exclude(module = "adventure-bom")
        exclude(module = "checker-qual")
        exclude(module = "annotations")
    }

    api("net.kyori:adventure-text-serializer-gson:4.16.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
        exclude(module = "gson")
    }

    api("net.kyori:adventure-text-serializer-legacy:4.16.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-serializer-plain:4.16.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-minimessage:4.16.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:event-api:3.0.0") {
        exclude(module = "checker-qual")
        exclude(module = "guava")
    }

    api("com.google.code.gson:gson:2.7")
    api("com.google.guava:guava:19.0")

    api("com.github.ben-manes.caffeine:caffeine:2.9.0")
    api("com.squareup.okhttp3:okhttp:3.14.9")
    api("com.squareup.okio:okio:1.17.5")
    api("net.bytebuddy:byte-buddy:1.10.22")

    api("org.spongepowered:configurate-core:3.7.2") {
        isTransitive = false
    }
    api("org.spongepowered:configurate-yaml:3.7.2") {
        isTransitive = false
    }
    api("org.spongepowered:configurate-gson:3.7.2") {
        isTransitive = false
    }
    api("org.spongepowered:configurate-hocon:3.7.2") {
        isTransitive = false
    }
    api("me.lucko.configurate:configurate-toml:3.7") {
        isTransitive = false
    }

    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("redis.clients:jedis:4.4.3")
    compileOnly("io.nats:jnats:2.16.4")
    compileOnly("com.rabbitmq:amqp-client:5.12.0")
    compileOnly("org.mongodb:mongodb-driver-legacy:4.5.0")
    compileOnly("org.postgresql:postgresql:42.6.0")
    compileOnly("org.yaml:snakeyaml:1.28")
}

tasks.shadowJar {
    archiveFileName = "multiversus-bukkit.jarinjar"

    dependencies {
        include(dependency("me.kubbidev.multiversus:.*"))
    }

    relocate("net.kyori.event", "me.kubbidev.multiversus.lib.eventbus")
    relocate("com.github.benmanes.caffeine", "me.kubbidev.multiversus.lib.caffeine")
    relocate("okio", "me.kubbidev.multiversus.lib.okio")
    relocate("okhttp3", "me.kubbidev.multiversus.lib.okhttp3")
    relocate("net.bytebuddy", "me.kubbidev.multiversus.lib.bytebuddy")
    relocate("me.lucko.commodore", "me.kubbidev.multiversus.lib.commodore")
    relocate("org.mariadb.jdbc", "me.kubbidev.multiversus.lib.mariadb")
    relocate("com.mysql", "me.kubbidev.multiversus.lib.mysql")
    relocate("org.postgresql", "me.kubbidev.multiversus.lib.postgresql")
    relocate("com.zaxxer.hikari", "me.kubbidev.multiversus.lib.hikari")
    relocate("com.mongodb", "me.kubbidev.multiversus.lib.mongodb")
    relocate("org.bson", "me.kubbidev.multiversus.lib.bson")
    relocate("redis.clients.jedis", "me.kubbidev.multiversus.lib.jedis")
    relocate("io.nats.client", "me.kubbidev.multiversus.lib.nats")
    relocate("com.rabbitmq", "me.kubbidev.multiversus.lib.rabbitmq")
    relocate("org.apache.commons.pool2", "me.kubbidev.multiversus.lib.commonspool2")
    relocate("ninja.leaping.configurate", "me.kubbidev.multiversus.lib.configurate")
}

artifacts {
    archives(tasks.shadowJar)
}