import com.github.gradle.node.npm.task.NpxTask
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.github.node-gradle.node") version "3.5.1"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jreleaser-playground"
            packageVersion = "1.0.0"
        }
    }
}

abstract class CommitlintTask : NpxTask() {
    @Input
    @Option(option="from", description="Lower end of the commit range.")
    var from = "HEAD~1"

    @Input
    @Option(option="to", description="Upper end of the commit range.")
    var to = "HEAD"

    init {
        command.set("@commitlint/cli")
        args.set(listOf("--from", from, "--to", to))
    }
    @TaskAction
    fun run() {
        super.exec()
    }
}

tasks.register<CommitlintTask>("commitlint")
