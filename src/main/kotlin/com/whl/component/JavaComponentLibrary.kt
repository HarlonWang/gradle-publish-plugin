package com.whl.component

import com.whl.ComponentLibrary
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

open class JavaComponentLibrary(private val project: Project) : ComponentLibrary() {

    override fun fromComponent(mavenPublication: MavenPublication) {
        mavenPublication.from(project.components.getByName("java"))
    }

    override fun docJar() : Any {
        val javadocJar = project.tasks.maybeCreate("javadocJar", Jar::class.java)
        javadocJar.from(project.tasks.getByName("javadoc"))
        javadocJar.classifier = "javadoc"
        return javadocJar
    }

    override fun sourcesJar() : Any{
        val sourcesJar = project.tasks.maybeCreate("sourcesJar", Jar::class.java)
        val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        sourcesJar.from(javaPluginConvention.sourceSets.getByName("main").allJava)
        sourcesJar.classifier = "sources"
        return sourcesJar
    }

}