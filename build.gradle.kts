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
version = file("VERSION").readText().trim()

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
            packageVersion = project.version.toString().removeSuffix("-SNAPSHOT")
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

                contributors {
                    format.set("- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}")
                }

                append {
                    enabled.set(true)
                }

                hide {
                    category("tasks")
                    category("build")
                    category("changes")
                    category("docs")
                }
            }
        }
    }

    distributions {
        register("app") {
            artifact {
                path.set(file("{{binariesDir}}/deb/jreleaser-playground_{{projectVersion}}-1_amd64.deb"))
            }

            artifact {
                path.set(file("{{binariesDir}}/dmg/jreleaser-playground-{{projectVersion}}.dmg"))
            }

            artifact {
                path.set(file("{{binariesDir}}/msi/jreleaser-playground-{{projectVersion}}.msi"))
            }
        }
    }
}
