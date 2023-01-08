import com.github.gradle.node.npm.task.NpxTask
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jreleaser.model.Active
import org.jreleaser.model.api.common.Apply

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.github.node-gradle.node") version "3.5.1"
    id("org.jreleaser") version "1.4.0"
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
    @Option(option = "from", description = "Lower end of the commit range.")
    var from = "HEAD~1"

    @Input
    @Option(option = "to", description = "Upper end of the commit range.")
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

jreleaser {
    environment {
        properties.set(
            mapOf(
                "binariesDir" to "build/compose/binaries/main"
            )
        )
    }
//    configFile.set(file(""))
    project {
        description.set("JReleaser test project.")
        license.set("Apache-2.0")
        authors.set(listOf("Martin Nonnenmacher"))
        copyright.set("Martin Nonnenmacher")
        inceptionYear.set("2022")

        links {
            homepage.set("https://github.com/mnonnenmacher/jreleaser-playground")
            documentation.set("https://github.com/mnonnenmacher/jreleaser-playground")
        }
    }

    release {
        github {
            repoOwner.set("mnonnenmacher")
            name.set("jreleaser-playground")
            discussionCategoryName.set("Announcements")
            overwrite.set(true)

            issues {
                enabled.set(true)
                comment.set("Release in {{tagName}} -> {{releaseNotesUrl}}")
                applyMilestone.set(Apply.NEVER)
            }

            changelog {
                links.set(true)
                formatted.set(Active.ALWAYS)
                preset.set("conventional-commits")

                append {
                    enabled.set(true)
                }
            }
        }
    }

    distributions {
        register("app") {
            artifact {
                path.set(file("{{binariesDir}}/msi/jreleaser-playground-1.0.0.deb"))
                path.set(file("{{binariesDir}}/msi/jreleaser-playground-1.0.0.dmg"))
                path.set(file("{{binariesDir}}/msi/jreleaser-playground-1.0.0.msi"))
            }
        }
    }
}
