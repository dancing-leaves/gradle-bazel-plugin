package org.jetbrains.bazel

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.bazel.exceptions.BazelTestExpectedRunFailException
import org.jetbrains.bazel.exceptions.BazelTestUnexpectedRunFailException
import org.jetbrains.bazel.tasks.BazelBuildTask
import org.jetbrains.bazel.tasks.BazelCleanTask
import org.jetbrains.bazel.tasks.BazelRunTask
import org.jetbrains.bazel.tasks.BazelTestTask
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.gradle.api.Project
import spock.lang.Specification


class BazelPluginTestSpec extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    private static final def GRADLE_BUILD_FILE_NAME = Project.DEFAULT_BUILD_FILE
    private static final def BAZEL_WORKSPACE_FILE_NAME = 'WORKSPACE'
    private static final def BAZEL_BUILD_FILE_NAME = 'BUILD'
    private static final def CC_TEST_FILE_NAME = "test.cc"

    private static final def GRADLE_BAZEL_TEST_TASK = 'bazel_task'
    private static final def GRADLE_CLEAN_TASK = 'bazel_clean'
    private static final def BAZEL_TARGET = 'bazel_test'

    private File gradleBuildFile
    private File bazelWorkspaceFile
    private File bazelBuildFile
    private File ccTestFile

    private static final String PLUGIN_INIT = """
            plugins {
                id '${BazelPlugin.package.name}'
            }
    """

    def setup() {
        gradleBuildFile = testProjectDir.newFile(GRADLE_BUILD_FILE_NAME)
        bazelWorkspaceFile = testProjectDir.newFile(BAZEL_WORKSPACE_FILE_NAME)
        bazelBuildFile = testProjectDir.newFile(BAZEL_BUILD_FILE_NAME)
        ccTestFile = testProjectDir.newFile(CC_TEST_FILE_NAME)
        bazelWorkspaceFile.createNewFile()
    }

    def getRunner(task) {
        GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withArguments([task])
    }

    private static def getSafePath(file) {
        file.canonicalPath.replace('\\', '/')
    }

    /**
     * Simple project build just to check if bazel
     * is available and environment is set properly
     */
    def simple_project_build_success() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).build()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.SUCCESS
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check if list options are extended correctly during
     * global to task configuration option inheritance.
     */
    def simple_project_build_with_combined_option() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            bazel {
                commandOptions = [
                    copt: [
                        '-DVAR1=true',
                        '-DVAR2=true',
                    ]
                ]
            }

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [
                    copt: [
                        '-DVAR3=true',
                        '-DVAR4=true',
                    ]
                ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() {
                return (VAR1 && VAR2 && VAR3 && VAR4);
            }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).build()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.SUCCESS
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check if global option gets overridden by task configuration
     */
    def simple_project_build_with_overridden_option() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            bazel {
                commandOptions = [
                    compilation_mode: 'opt'
                ]
            }

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                commandOptions = [
                    compilation_mode: 'wrong_mode'
                ]
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() {
                return (VAR1 && VAR2 && VAR3 && VAR4);
            }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).buildAndFail()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.FAILED
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check if configuration failed on non consistent
     * (different type) option between global and task
     */
    def simple_project_build_with_inconsistent_option() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            bazel {
                commandOptions = [
                    compilation_mode: 'opt'
                ]
            }

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [
                    compilation_mode: 1
                ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).buildAndFail()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.FAILED
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check build failure
     */
    def simple_project_build_failure() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return meow; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).buildAndFail()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.FAILED
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check customization of bazel executable
     */
    def simple_project_global_bazel_unset() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            bazel {
                executable = 'not_bazel_mew'
            }

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                executable = 'bazel'
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).buildAndFail()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.FAILED
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check test success
     */
    def simple_project_test_success() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelTestTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_test(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).build()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.SUCCESS
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check test failure
     */
    def simple_project_test_failed() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelTestTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_test(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 1; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).buildAndFail()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.FAILED
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check failure if no tests are configured
     */
    def simple_project_test_not_found() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelTestTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).buildAndFail()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.FAILED
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check run success
     */
    def simple_project_run_success() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelRunTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                target = '${BAZEL_TARGET}'
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).build()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.SUCCESS
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check run failure
     */
    def simple_project_run_failed() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelRunTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                target = '${BAZEL_TARGET}'
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return ${BazelTestExpectedRunFailException.EXIT_CODE}; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).buildAndFail()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.FAILED
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check setting of environment variables (global & task)
     */
    def complex_project_run_with_environment() {
        given:
        gradleBuildFile << """\
            ${PLUGIN_INIT}

            bazel {
                environment = [
                    ENV_VAR1: 'woof'
                ]
            }

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelRunTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                target = '${BAZEL_TARGET}'
                environment = [
                    ENV_VAR2: 'meow'
                ]
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            #include <stdlib.h>
            int main() {
                if (!getenv("ENV_VAR1")) return ${BazelTestUnexpectedRunFailException.EXIT_CODE};
                if (!getenv("ENV_VAR2")) return ${BazelTestUnexpectedRunFailException.EXIT_CODE};
                return 0;
            }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).build()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.SUCCESS
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check build of multiple specific targets
     */
    def complex_project_build_two_targets() {
        given:
        def secondTarget = 'second_target'
        def thirdTarget = 'third_target'

        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                targets = ['${BAZEL_TARGET}', '${secondTarget}']
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )

            cc_binary(
                name = '${secondTarget}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )

            cc_binary(
                name = '${thirdTarget}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).build()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.SUCCESS
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }

    /**
     * Check build of default (all) targets
     */
    def complex_project_build_default_all_targets() {
        given:
        def secondTarget = 'second_target'
        def thirdTarget = 'third_target'

        gradleBuildFile << """\
            ${PLUGIN_INIT}

            task ${GRADLE_BAZEL_TEST_TASK}(type: ${BazelBuildTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
            }

            task ${GRADLE_CLEAN_TASK}(type: ${BazelCleanTask.class.name}) {
                workspace = file('${getSafePath(testProjectDir.getRoot())}')
                startupOptions = [ ignore_all_rc_files: true ]
                commandOptions = [ expunge: true ]
            }\
        """.stripIndent()

        bazelBuildFile << """\
            cc_binary(
                name = '${BAZEL_TARGET}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )

            cc_binary(
                name = '${secondTarget}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )

            cc_binary(
                name = '${thirdTarget}',
                srcs = ['${CC_TEST_FILE_NAME}'],
            )\
        """.stripIndent()

        ccTestFile << """\
            int main() { return 0; }\
        """.stripIndent()

        when:
        def buildResult = null
        def cleanResult = null

        try {
            buildResult = getRunner(GRADLE_BAZEL_TEST_TASK).build()
        } finally {
            cleanResult = getRunner(GRADLE_CLEAN_TASK).build()
        }

        then:
        buildResult.task(":${GRADLE_BAZEL_TEST_TASK}").outcome == TaskOutcome.SUCCESS
        cleanResult.task(":${GRADLE_CLEAN_TASK}").outcome == TaskOutcome.SUCCESS
    }
}
