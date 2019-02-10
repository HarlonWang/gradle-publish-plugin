package com.whl

import org.gradle.api.publish.maven.MavenPublication

/**
 * Only support publish kotlin or android or java library or mix library with kotlin project, otherwise see
 */
abstract class ComponentLibrary {

    fun buildComponentLibrary(mavenPublication: MavenPublication, extension: GradlePublishExtension) {
        fromComponent(mavenPublication)
        if (extension.sourceJarEnabled) {
            mavenPublication.artifact(sourcesJar())
        }
        if (extension.javaDocEnabled) {
            mavenPublication.artifact(docJar())
        }
        withPom(mavenPublication)
    }

    abstract fun fromComponent(mavenPublication: MavenPublication)

    abstract fun docJar() : Any

    abstract fun sourcesJar() : Any

    open fun withPom(mavenPublication: MavenPublication) {}

}