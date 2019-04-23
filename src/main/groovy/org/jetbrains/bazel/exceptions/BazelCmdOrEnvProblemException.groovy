package org.jetbrains.bazel.exceptions

abstract class BazelExecutionException extends Exception {

    BazelExecutionException(errorMessage) {
        super(errorMessage)
    }

    /**
     * Exceptions for some exit codes of bazel:
     * https://docs.bazel.build/versions/master/guide.html#what-exit-code-will-i-get
     */
    final static def EXIT_CODE_EXCEPTION_MAPPING = [
        1: BazelExecutionBuildFailedException,
        2: BazelExecutionCmdOrEnvProblemException,
        3: BazelExecutionTestsFailedException,
        4: BazelExecutionTestsNotFoundException,
        (BazelTestExpectedRunFailException.EXIT_CODE): BazelTestExpectedRunFailException,
        (BazelTestUnexpectedRunFailException.EXIT_CODE): BazelTestUnexpectedRunFailException,
    ]

    static def getException(exitCode, errorMessage) {
        if (EXIT_CODE_EXCEPTION_MAPPING.containsKey(exitCode))
            return EXIT_CODE_EXCEPTION_MAPPING[exitCode].newInstance(errorMessage)

        new BazelExecutionOtherException(exitCode, errorMessage)
    }
}

class BazelExecutionCmdOrEnvProblemException
        extends BazelExecutionException {

    BazelExecutionCmdOrEnvProblemException(errorMessage) {
        super(errorMessage)
    }
}

class BazelExecutionBuildFailedException
        extends BazelExecutionException {

    BazelExecutionBuildFailedException(errorMessage) {
        super(errorMessage)
    }
}

class BazelExecutionTestsFailedException
        extends BazelExecutionException {

    BazelExecutionTestsFailedException(errorMessage) {
        super(errorMessage)
    }
}

class BazelExecutionTestsNotFoundException
        extends BazelExecutionException {

    BazelExecutionTestsNotFoundException(errorMessage) {
        super(errorMessage)
    }
}

class BazelExecutionOtherException
        extends BazelExecutionException {
    def exitCode

    BazelExecutionOtherException(exitCode, errorMessage) {
        super(errorMessage)
        this.exitCode = exitCode
    }
}


/**
 * Some reserved exceptions for testing
 */

class BazelTestExpectedRunFailException
        extends BazelExecutionException {
    final static def EXIT_CODE = 200

    BazelTestExpectedRunFailException(errorMessage) {
        super(errorMessage)
    }
}

class BazelTestUnexpectedRunFailException
        extends BazelExecutionException {
    final static def EXIT_CODE = 201

    BazelTestUnexpectedRunFailException(errorMessage) {
        super(errorMessage)
    }
}