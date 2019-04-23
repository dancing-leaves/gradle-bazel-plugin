package org.jetbrains.bazel.tasks

import org.gradle.api.plugins.BasePlugin

/**
 * Wrapper task for bazel build command
 * https://docs.bazel.build/versions/master/command-line-reference.html#build
 */
abstract class BazelBuildTask extends BazelMultiTargetTask {
    final static private String BAZEL_BUILD_COMMAND = 'build'
    final static private String GRADLE_BUILD_TASK = BasePlugin.BUILD_GROUP

    BazelBuildTask() {
        super()
        project.getTasksByName(GRADLE_BUILD_TASK, true).each { it.dependsOn(this) }
    }

    @Override
    String getCommand() {
        BAZEL_BUILD_COMMAND
    }
}
