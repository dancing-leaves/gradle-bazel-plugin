package org.jetbrains.bazel

import org.gradle.api.Plugin
import org.gradle.api.Project

class BazelPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create(BazelPluginExtension.EXTENSION_NAME, BazelPluginExtension)
//        project.tasks.create('bazel_task', BazelExecutorTask)
    }
}