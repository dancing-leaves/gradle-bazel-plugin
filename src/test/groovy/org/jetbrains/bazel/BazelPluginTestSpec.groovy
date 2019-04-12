package org.jetbrains.bazel

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import spock.lang.Shared
import org.gradle.api.plugins.BasePlugin
import org.junit.rules.TemporaryFolder
import org.gradle.api.Project
import spock.lang.Specification


class BazelPluginTestSpec extends Specification {
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

            bazel {
                executable = 'meow'
                options = [
                    'one': 1,
                    'two': 'woof',
                    'three': [ 'a', 'ac', 'av'],
                ]

            }

//            task bazel_build(type: ${BazelExecutorTask.class.name}) {
//                workspace = file('C:/Development/jetbrains/examples/cpp-tutorial/stage1')
//                executable = 'C:/Development/jetbrains/tools/bazel.exe'
//                options = [
//                    'one': 1,
//                    'two': 'woof',
//                ]
//            }
        """

        when:
        def result = build()

        then:
        true
    }

    BuildResult build(task = BasePlugin.BUILD_GROUP) {
        return GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments([task])
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
