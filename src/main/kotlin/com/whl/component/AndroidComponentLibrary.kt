package com.whl.component

import com.android.build.gradle.LibraryExtension
import com.whl.ComponentLibrary
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import java.io.File

class AndroidComponentLibrary(private val project: Project) : ComponentLibrary() {

    private val android: LibraryExtension = project.extensions.getByName("android") as LibraryExtension

    override fun fromComponent(mavenPublication: MavenPublication) {
        fromAndroidComponent(mavenPublication)
    }

    private fun fromAndroidComponent(mavenPublication: MavenPublication) {
        var bundleReleaseAar: Task? = null
        if (project.tasks.findByName("bundleReleaseAar") != null) {
            bundleReleaseAar = project.tasks.getByName("bundleReleaseAar")
        }
        //we only use bundleRelease in lower android gradle plugin version such as 2.3.3
        //more information look this https://stackoverflow.com/questions/51433769/why-android-gradle-maven-publish-artifact-bundlerelease-not-found/51869825#51869825
        if (project.tasks.findByName("bundleRelease") != null) {
            bundleReleaseAar = project.tasks.getByName("bundleRelease")
        }
        mavenPublication.artifact(bundleReleaseAar)
    }

    /**
     * Reference on https://github.com/JakeWharton/dagger-reflect/blob/master/gradle/gradle-mvn-push.gradle
     */
    override fun docJar(): Any {
        val androidJavaDocs = project.tasks.create("androidJavadocs", Javadoc::class.java)
        androidJavaDocs.setSource(android.sourceSets.getByName("main").java.srcDirs)
        androidJavaDocs.classpath += project.files("${android.bootClasspath}${File.pathSeparator}")

        val androidJavaDocsJar = project.tasks.create("androidJavaDocsJar", Jar::class.java)
        androidJavaDocsJar.classifier = "javadoc"
        androidJavaDocsJar.from(androidJavaDocs.destinationDir)
        androidJavaDocsJar.dependsOn(androidJavaDocs)
        return androidJavaDocsJar
    }

    override fun sourcesJar(): Any {
        val androidSourcesJar = project.tasks.create("androidSourcesJar", Jar::class.java)
        androidSourcesJar.from(android.sourceSets.getByName("main").java.srcDirs)
        androidSourcesJar.classifier = "sources"
        return androidSourcesJar
    }

    override fun withPom(mavenPublication: MavenPublication) {
        mavenPublication.pom.withXml { xmlProvider ->
            val dependenciesNode = xmlProvider.asNode().appendNode("dependencies")
            fun addDependency(dep: Dependency, scope: String) {
                if (dep.group == null || dep.version == null || dep.name == "unspecified") {
                    return // ignore invalid dependencies
                }
                //currently we only handle the dependency implements ModuleDependency interface
                // for support more feature, such as excludeRules
                if (dep is ModuleDependency) {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.apply {
                        appendNode("groupId", dep.group)
                        appendNode("artifactId", dep.name)
                        appendNode("version", dep.version)
                        appendNode("scope", scope)
                        dep.artifacts.forEach {depArtifact ->
                            appendNode("type", depArtifact.type)
                        }
                    }

                    val exclusionsNode = dependencyNode.appendNode("exclusions")
                    when {
                        !dep.isTransitive -> { // If this dependency is not transitive, we should force exclude all its dependencies them from the POM
                            val exclusionNode = exclusionsNode.appendNode("exclusion")
                            exclusionNode.apply {
                                appendNode("groupId", "*")
                                appendNode("artifactId", "*")
                            }
                        }
                        !dep.excludeRules.isEmpty() -> // Otherwise add specified exclude rules
                            dep.excludeRules.forEach {rule ->
                                val exclusionNode = exclusionsNode.appendNode("exclusion")
                                exclusionNode.apply {
                                    appendNode("groupId", rule.group ?: "*")
                                    appendNode("artifactId", rule.module ?: "*")
                                }
                            }
                        else -> {
                            //do nothing
                        }
                    }
                }
            }

            // List all "compile" dependencies (for old Gradle)
            project.configurations.getByName("compile").dependencies.forEach { dep ->
                addDependency(
                    dep,
                    "compile"
                )
            }

            //support api & implementation configuration until gradle version 3.4
            if (project.configurations.findByName("api") != null) {
                // List all "api" dependencies (for new Gradle) as "compile" dependencies
                project.configurations.getByName("api").dependencies.forEach {dep -> addDependency(dep, "api") }
            }

            if (project.configurations.findByName("implementation") != null) {
                // List all "implementation" dependencies (for new Gradle) as "runtime" dependencies
                project.configurations.getByName("implementation").dependencies.forEach {dep -> addDependency(dep, "runtime") }
            }
        }
    }

}