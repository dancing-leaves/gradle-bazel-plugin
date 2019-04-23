package org.jetbrains.bazel.exceptions

class BazelProcessStartException extends Exception {

    BazelProcessStartException(errorMessage) {
        super(errorMessage)
    }
}
