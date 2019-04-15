package org.jetbrains.bazel

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

class BazelExecutorConfiguration {
    static final def DEFAULT_EXECUTABLE = 'bazel'
    static final def DEFAULT_COMMAND = 'build'

    DirectoryProperty workspace
    Property<String> executable
    MapProperty<String,Object> arguments
    Property<String> command
    MapProperty<String,Object> options
//    Property<String> targets
    MapProperty<String,String> environmant

//    @Input
//    protected List<String> getPaths() {
//        return collect(getPluginClasspath(), new Transformer<String, File>() {
//            @Override
//            public String transform(File file) {
//                return file.getAbsolutePath().replaceAll("\\\\", "/");
//            }
//        });
//    }

//    def getExecutable() {
//        if(executable.present) {
//            executable
//        }
//
//        DEFAULT_EXECUTABLE
//    }


//    def getCommand() {
//        if(command.present) {
//            command
//        }
//
//        DEFAULT_COMMAND
//    }
//
//    def setArguments(arguments) {
//        println "setter ${arguments}"
//    }

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
