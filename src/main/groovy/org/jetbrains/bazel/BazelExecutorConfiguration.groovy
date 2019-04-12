package org.jetbrains.bazel

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

class Option {
    def value

    Option(value) {
        this.value = value
    }
}

class BazelExecutorConfiguration {
    static final def DEFAULT_EXECUTABLE = 'bazel'
    static final def DEFAULT_COMMAND = 'build'

    DirectoryProperty workspace
    Property<String> executable
    MapProperty<String,Option> arguments
    Property<String> command
    MapProperty<String,Option> options

//    def getExecutable() {
//        if(executable.present) {
//            executable
//        }
//
//        DEFAULT_EXECUTABLE
//    }


    def getCommand() {
        if(command.present) {
            command
        }

        DEFAULT_COMMAND
    }

    def setArguments(arguments) {
        println arguments
    }

//    def setOptions(options) {
//        println options
//    }

    def getProcessBuilder() {
        return new ProcessBuilder()
            .directory(
                workspace
            )
            .command([
                executable,
                *options,
                command,
                *arguments,
            ])
    }
}