plugins {
    id("java-library")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.pop4959.srbot"
version = project.property("version")!!

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:${project.property("jdaVersion")}")
    implementation("com.ibasco.agql:agql-steam-webapi:1.2.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:none")
    }
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = "com.github.pop4959.srbot.Main"
        }
    }
    build {
        dependsOn(shadowJar)
    }
}
