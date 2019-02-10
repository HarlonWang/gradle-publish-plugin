package com.whl

import com.whl.component.AndroidComponentLibrary
import com.whl.component.JavaComponentLibrary
import com.whl.component.KotlinComponentLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import org.gradle.util.GradleVersion
import java.lang.RuntimeException

open class GradlePublishPlugin : Plugin<Project>{

    override fun apply(project: Project) {
        val extension: GradlePublishExtension = createExtension(project)
        project.afterEvaluate {
            configurePublishing(project, extension)
            if (GradleVersion.current() >= GradleVersion.version("4.8") && extension.signEnabled) {
                configureSigning(project)
            }
        }
    }

    private fun createExtension(project: Project) = project.extensions.create("gradlePublish", GradlePublishExtension::class.java, project)

    private fun configurePublishing(project: Project, extension: GradlePublishExtension) {
        project.plugins.apply("maven-publish")

        project.plugins.withId("maven-publish") {
            val version = project.version as String

            project.extensions.configure(PublishingExtension::class.java) { publishing ->
                publishing.publications { publication ->
                    publication.create("maven", MavenPublication::class.java) { maven ->
                        maven.version = version
                        project.componentLibrary().buildComponentLibrary(maven, extension)
                    }
                }
                publishing.repositories {repository ->
                    val (url, userName, password) = if (version.endsWith("-SNAPSHOT")) extension.snapshotRepository else extension.releaseRepository
                    repository.maven {mavenRepository ->
                        mavenRepository.setUrl(url)
                        mavenRepository.credentials { credential ->
                            credential.username = userName
                            credential.password = password
                        }
                    }
                }
            }
        }
    }

    private fun configureSigning(project: Project) {
        project.plugins.apply("signing")
        project.plugins.withId("signing") {
            project.extensions.configure(SigningExtension::class.java) {signing ->
                val publishing = project.extensions.getByName("publishing") as PublishingExtension
                signing.sign(publishing.publications.getByName("maven"))
            }
        }
    }

}

fun Project.componentLibrary() : ComponentLibrary = when {
    plugins.hasPlugin("java-library") && !plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> JavaComponentLibrary(project)
    plugins.hasPlugin("com.android.library") -> AndroidComponentLibrary(project)
    plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> KotlinComponentLibrary(project)
    else -> throw RuntimeException("This project was unsupported, please make sure one of apply java-library or com.android.library or org.jetbrains.kotlin.jvm !")
}