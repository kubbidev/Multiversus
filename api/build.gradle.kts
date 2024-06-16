group = "net.multiversus"
version = "1.0"

tasks.jar {
    manifest {
        attributes(Pair("Automatic-Module-Name", "net.multiversus.api"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "api"

            from(components["java"])
            pom {
                name = "Multiversus API"
                description = "A Minecraft plugin where every match is a new wild party of iconic characters challenging each other in wonderfully strange ways."
                url = "https://kubbidev.com"

                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }

                developers {
                    developer {
                        id = "kubbidev"
                        name = "kubbi"
                        url = "https://kubbidev.com"
                        email = "kubbidev@gmail.com"
                    }
                }

                issueManagement {
                    system = "Github"
                    url = "https://github.com/kubbidev/Multiversus/issues"
                }
            }
        }
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
}