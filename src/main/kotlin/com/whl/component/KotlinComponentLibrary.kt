package com.whl.component

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaPlugin

class KotlinComponentLibrary(private val project: Project) : JavaComponentLibrary(project){

    init {
        applyDokkaPlugin()
    }

    private fun applyDokkaPlugin() {
        //No more duplicate apply plugin if already had
        if (!project.plugins.hasPlugin("org.jetbrains.dokka")) {
            project.plugins.apply(DokkaPlugin::class.java)
        }
    }

    override fun docJar(): Any {
        val dokka = project.tasks.getByName("dokka")
        dokka.setProperty("outputFormat", "html")
        dokka.setProperty("outputDirectory", "${project.buildDir}/javadoc")

        val dokkaJar = project.tasks.maybeCreate("dokkaJar", Jar::class.java)
        dokkaJar.group = JavaBasePlugin.DOCUMENTATION_GROUP
        dokkaJar.description = "Assembles Kotlin docs with Dokka"
        dokkaJar.classifier = "javadoc"
        dokkaJar.from(dokka)
        return dokkaJar
    }

}