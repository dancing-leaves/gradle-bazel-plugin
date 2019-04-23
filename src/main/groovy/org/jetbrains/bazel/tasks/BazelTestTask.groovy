package org.jetbrains.bazel.tasks

/**
 * Wrapper task for bazel test command
 * https://docs.bazel.build/versions/master/command-line-reference.html#test
 */
abstract class BazelTestTask extends BazelMultiTargetTask {
    final static private String BAZEL_TEST_COMMAND = 'test'
    final static private String GRADLE_TEST_TASK = 'test'

    BazelTestTask() {
        super()
        project.getTasksByName(GRADLE_TEST_TASK, true).each { it.dependsOn(this) }
    }

    @Override
    String getCommand() {
        BAZEL_TEST_COMMAND
    }
}
