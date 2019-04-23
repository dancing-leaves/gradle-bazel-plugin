package org.jetbrains.bazel.tasks

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

/**
 * Parent for all multi target bazel commands such as build and test
 */
abstract class BazelMultiTargetTask extends BazelTask {
    private final static def DEFAULT_TARGETS = ['...']

    @Input
    final ListProperty<String> targets

    BazelMultiTargetTask() {
        super()
        targets = project.objects.listProperty(String)
        targets.convention(this.DEFAULT_TARGETS)
    }

    @Override
    def generateProcessBuilderCommand() {
        [
            *super.generateProcessBuilderCommand(),
            *targets.get()
        ]
    }
}
