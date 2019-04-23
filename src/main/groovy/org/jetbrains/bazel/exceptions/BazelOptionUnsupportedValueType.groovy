package org.jetbrains.bazel.exceptions

class BazelOptionUnsupportedValueType extends Exception {
    private def clazz

    BazelOptionUnsupportedValueType(clazz) {
        super("Unsupported bazel option type: ${clazz}")
        this.clazz = clazz
    }
}