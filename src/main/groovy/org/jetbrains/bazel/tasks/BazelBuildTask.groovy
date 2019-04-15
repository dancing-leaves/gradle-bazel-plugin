package org.jetbrains.bazel.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.jetbrains.bazel.exceptions.BazelExecutionException


abstract class BazelBuildTask extends DefaultTask {
    final static private String DEFAULT_BAZEL_EXECUTABLE = 'bazel'
    final static private String BAZEL_BUILD_COMMAND = 'build'

    @Input
    final Property<String> executable

    @InputDirectory
    final DirectoryProperty workspace

    @Input
    final MapProperty<String,Object> startupOptions

    @Input
    final MapProperty<String,Object> commandOptions

    @Input
    final ListProperty<String> targets

    @Input
    final MapProperty<String,String> environment

    def getCommand() {
        BAZEL_BUILD_COMMAND
    }

    BazelBuildTask() {
        executable = project.objects.property(String)
        workspace = project.objects.directoryProperty()
        startupOptions = project.objects.mapProperty(String, Object)
        commandOptions = project.objects.mapProperty(String, Object)
        targets = project.objects.listProperty(String)
        environment = project.objects.mapProperty(String, String)

        executable.convention(DEFAULT_BAZEL_EXECUTABLE)
    }

    @TaskAction
    def run() {
        def processBuilder = generateProcessBuilder()
        def process = processBuilder.start()

        def stdout = new BufferedReader(new InputStreamReader(process.inputStream))
        def stderr = new BufferedReader(new InputStreamReader(process.errorStream))

        def exitCode = process.waitFor()

        logger.info(stdout.readLines().join('\n'))

        if(exitCode)
            throw new BazelExecutionException(exitCode, stderr.readLines().join('\n'))
    }

    private def generateProcessBuilder() {
        def bazelExecutable = DEFAULT_BAZEL_EXECUTABLE

        if (executable.isPresent())
            bazelExecutable = executable.get()

        def processBuilder = new ProcessBuilder()
                .directory(
                        workspace.get().asFile
                )
                .command([
                        bazelExecutable,
                        *generateArgsForOptions(startupOptions),
                        command,
                        *generateArgsForOptions(commandOptions),
                        '--',
                        *targets.get(),
                ])

        def environment = processBuilder.environment()
        environment.putAll(this.environment.get())

        processBuilder
    }

    private def generateArgsForOptions(propertyMap) {
        def args = []

        propertyMap.get().each { k, v ->
            if (v instanceof Boolean) {
                args << "--${v ? '' : 'no'}${k}"
            } else if (v instanceof ArrayList) {
                for (a in v) args << "--${k}=${a}"
            } else {
                args << "--${k}=${v}"
            }
        }

        args.collect {it.toString()}
    }
}
