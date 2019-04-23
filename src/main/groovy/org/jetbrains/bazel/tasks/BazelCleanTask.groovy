package org.jetbrains.bazel.tasks

import org.gradle.api.plugins.BasePlugin

/**
 * Wrapper task for bazel clean command
 * https://docs.bazel.build/versions/master/command-line-reference.html#clean
 */
abstract class BazelCleanTask extends BazelTask {
    final static private String BAZEL_CLEAN_COMMAND = 'clean'
    final static private String GRADLE_BUILD_TASK = BasePlugin.CLEAN_TASK_NAME

    BazelCleanTask() {
        super()
        project.getTasksByName(GRADLE_BUILD_TASK, true).each { it.dependsOn(this) }
    }

    @Override
    String getCommand() {
        BAZEL_CLEAN_COMMAND
    }
}
