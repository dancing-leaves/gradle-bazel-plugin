package org.jetbrains.bazel.tasks

abstract class BazelTestTask extends BazelBuildTask {
    final static private String BAZEL_TEST_COMMAND = 'test'

    @Override
    def getCommand() {
        BAZEL_TEST_COMMAND
    }
}
