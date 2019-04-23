package org.jetbrains.bazel.tasks

import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * Wrapper task for bazel run command
 * https://docs.bazel.build/versions/master/command-line-reference.html#run
 */
abstract class BazelRunTask extends BazelTask {
    final static private String BAZEL_RUN_COMMAND = 'run'
    final static private String GRADLE_RUN_TASK = ApplicationPlugin.TASK_RUN_NAME

    @Input
    final Property<String> target

    BazelRunTask() {
        super()
        target = project.objects.property(String)
        project.getTasksByName(GRADLE_RUN_TASK, true).each { it.dependsOn(this) }
    }

    @Override
    String getCommand() {
        BAZEL_RUN_COMMAND
    }

    @Override
    def generateProcessBuilderCommand() {
        [
                *super.generateProcessBuilderCommand(),
                target.get()
        ]
    }
}
