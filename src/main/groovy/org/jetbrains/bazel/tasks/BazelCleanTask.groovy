package org.jetbrains.bazel.tasks

abstract class BazelCleanTask extends BazelBuildTask {
    final static private String BAZEL_CLEAN_COMMAND = 'clean'

    @Override
    def getCommand() {
        BAZEL_CLEAN_COMMAND
    }
}
