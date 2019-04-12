package org.jetbrains.bazel

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask


abstract class BazelExecutorTask extends DefaultTask {

    @InputDirectory
    final DirectoryProperty workspace

    @InputFile
    final Property<String> executable

    @Input
    final MapProperty<String,Option> options

    @Input
    final Property<String> command

    @Input
    final MapProperty<String,Option> arguments

    BazelExecutorTask() {
        workspace = project.objects.directoryProperty()
        executable = project.objects.property(String)
        options = project.objects.mapProperty(String, Option)
        command = project.objects.property(String)
        arguments = project.objects.mapProperty(String, Option)

//        def extension = project.extensions.getByName(BazelPlugin.EXTENSION_NAME)
//
//        workspace.set(extension.workspace)
//        executable.set(extension.executable)
//        command.set(extension.command)
//        options.set(extension.options)
//        arguments.set(extension.arguments)
    }

    @TaskAction
    def run() {
        println 'meow'
    }
}