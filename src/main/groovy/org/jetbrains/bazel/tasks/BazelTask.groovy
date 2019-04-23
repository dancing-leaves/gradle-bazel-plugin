package org.jetbrains.bazel.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.jetbrains.bazel.BazelPluginExtension
import org.jetbrains.bazel.exceptions.BazelExecutionException
import org.jetbrains.bazel.exceptions.BazelProcessStartException
import org.jetbrains.bazel.utils.BazelOption

/**
 * Parent for all bazel command wrapper tasks
 */
abstract class BazelTask extends DefaultTask {

    @Input
    final Property<String> executable

    @InputDirectory
    final DirectoryProperty workspace

    @Input
    final MapProperty<String,Object> startupOptions

    @Input
    final MapProperty<String,Object> commandOptions

    @Input
    final MapProperty<String,String> environment

    @Input
    final MapProperty<String,Object> globalCommandOptions

    @Input
    final MapProperty<String,String> globalEnvironment

    @Input
    final MapProperty<String,Object> globalStartupOptions

    @Input
    abstract String getCommand()

    BazelTask() {
        executable = project.objects.property(String)
        workspace = project.objects.directoryProperty()
        startupOptions = project.objects.mapProperty(String, Object)
        commandOptions = project.objects.mapProperty(String, Object)
        environment = project.objects.mapProperty(String, String)

        globalStartupOptions = project.objects.mapProperty(String, Object)
        globalCommandOptions = project.objects.mapProperty(String, Object)
        globalEnvironment = project.objects.mapProperty(String, String)

        def bazelPluginExtension = project.extensions.getByName(
                BazelPluginExtension.EXTENSION_NAME) as BazelPluginExtension

        globalStartupOptions.set(bazelPluginExtension.startupOptions)
        globalCommandOptions.set(bazelPluginExtension.commandOptions)
        globalEnvironment.set(bazelPluginExtension.environment)

        executable.convention(bazelPluginExtension.executable)
    }

    /**
     * Gradle task logic
     */
    @TaskAction
    def run() {
        def process

        try {
            process = generateProcessBuilder().start()
        } catch(IOException exception) {
            throw new BazelProcessStartException(exception.message)
        }

        def stdout = new BufferedReader(new InputStreamReader(process.inputStream))
        def stderr = new BufferedReader(new InputStreamReader(process.errorStream))

        def exitCode = process.waitFor()
        logger.info(stdout.readLines().join('\n'))
        def errorMessage = stderr.readLines().join('\n')

        if (exitCode)
            throw BazelExecutionException.getException(exitCode, errorMessage)
    }

    /**
     * Generate process builder ready for start
     *
     * @return process builder instance
     */
    private def generateProcessBuilder() {
        def processBuilder = new ProcessBuilder()
                .directory(workspace.get().asFile)
                .command(generateProcessBuilderCommand())

        def processEnvironment = processBuilder.environment()
        processEnvironment.putAll(globalEnvironment.get())
        processEnvironment.putAll(environment.get())

        processBuilder
    }

    /**
     * Generate valid bazel command ready for execution
     *
     * @return complete set of bazel command line arguments
     */
    def generateProcessBuilderCommand() {
        [
                executable.get(),
                *BazelOption.combine(
                        BazelOption.getOptions(startupOptions),
                        BazelOption.getOptions(globalStartupOptions),
                ).collect { it.cmdArgs }.flatten(),
                command,
                *BazelOption.combine(
                        BazelOption.getOptions(commandOptions),
                        BazelOption.getOptions(globalCommandOptions),
                ).collect { it.cmdArgs }.flatten(),
                '--',
        ]
    }
}
