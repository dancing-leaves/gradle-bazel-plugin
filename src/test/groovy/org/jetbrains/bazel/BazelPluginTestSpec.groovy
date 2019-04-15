package org.jetbrains.bazel

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.jetbrains.bazel.tasks.BazelBuildTask
import org.junit.Rule
import spock.lang.Shared
import org.gradle.api.plugins.BasePlugin
import org.junit.rules.TemporaryFolder
import org.gradle.api.Project
import spock.lang.Specification


class BazelPluginTestSpec extends Specification {
    private final def TEST_TARGET = 'bazel_build'
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    private File buildFile

    @Shared File workspace = new File('src/test/resources/library')
    @Shared File executable

    private static final String pluginInit = """
        plugins {
            id '${BazelPlugin.package.name}'
        }
    """

    def setupSpec() {
        // workspace = new File('src/test/resources/library')
    }

    def setup() {
        buildFile = testProjectDir.newFile(Project.DEFAULT_BUILD_FILE)
    }

    def test1() {
        given:
        buildFile << """
            ${pluginInit}

//            bazel {
//                executable = 'meow'
//                options = [
//                    'one': '1,
//                    'two': 'woof',
//                    'three': [ 'a', 'ac', 'av'],
//                ]
//            }
                
            task bazel_build(type: ${BazelBuildTask.class.name}) {
                workspace = file('C:/Development/jetbrains/examples/cpp-tutorial/stage1')

                startupOptions = [
                    'max_idle_secs': 10000,
                    'connect_timeout_secs': '60',
                ]

                commandOptions = [
                    'experimental_docker_image': '',
                    'experimental_scale_timeout': '1.0',
                ]
                
                
                targets = ['...']
            }
        """

        when:
        def result = build('bazel_build')

        then:
        true
    }

    BuildResult build(task = BasePlugin.BUILD_GROUP) {
        return GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withArguments(['--info', task])
                .forwardOutput()
                .build()
    }

    // def test2() {
    //     given:

    //     when:
    //     def result = build()

    //     then:
    //     true
    // }
}
