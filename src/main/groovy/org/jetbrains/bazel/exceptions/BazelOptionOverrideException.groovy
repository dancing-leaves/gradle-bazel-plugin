package org.jetbrains.bazel.exceptions

class BazelOptionOverrideException extends Exception {
    private def optionThis
    private def optionOther

    BazelOptionOverrideException(optionThis, optionOther) {
        super(
                "Option ${optionThis.key} has different types in " +
                "conigurations: ${optionThis.value.class} vs ${optionOther.value.class}"
        )

        this.optionThis = optionThis
        this.optionOther = optionOther
    }
}