package com.whl

import org.gradle.api.Action
import org.gradle.api.Project

open class GradlePublishExtension(project: Project) {

    /**
     * The release repository url this should be published to.
     */
    private val defaultReleaseUrl: String = "${project.buildDir}/repository/releases"
    /**
     * The snapshot repository url this should be published to.
     */
    private val defaultSnapshotUrl: String = "${project.buildDir}/repository/snapshots"

    /**
     * The source code should be published default, otherwise not
     */
    var sourceJarEnabled = true

    /**
     * The Signing Plugin is used to generate a signature file for each artifact.
     * Since Gradle Version 4.8
     */
    var signEnabled = false

    /**
     * The java doc should be published default, otherwise not
     */
    var javaDocEnabled = false

    /**
     * The release repository this should be set to.
     */
    var releaseRepository = MavenRepository(url = defaultReleaseUrl)
    /**
     * The snapshot repository this should be set to.
     */
    var snapshotRepository = MavenRepository(url = defaultSnapshotUrl)

    fun releaseRepository(action: Action<MavenRepository>) {
        action.execute(releaseRepository)
    }

    fun snapshotRepository(action: Action<MavenRepository>) {
        action.execute(snapshotRepository)
    }

    data class MavenRepository(
        /**
         * The repository url this should be published to.
         */
        var url: String,
        /**
         * The userName that should be used for publishing.
         */
        var userName: String = "",
        /**
         * The password that should be used for publishing.
         */
        var password: String = ""
    )

}


