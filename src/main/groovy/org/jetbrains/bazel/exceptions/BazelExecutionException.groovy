package org.jetbrains.bazel.exceptions

class BazelExecutionException extends Exception {
    private def exitCode

    BazelExecutionException(exitCode, message) {
        super("\n${message}")
        this.exitCode = exitCode
    }
}