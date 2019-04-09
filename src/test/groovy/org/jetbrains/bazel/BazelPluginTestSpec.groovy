package org.jetbrains.bazel

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BazelPluginTestSpec extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    private File settingsFile;
    private File buildFile;

    def setupSpec() {

    }

    def setup() {

    }

    BuildResult build() {
        return GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .build()
    }

    def test1() {
        given:

        when:
        def result = build()

        then:
        true
    }

    def test2() {
        given:

        when:
        def result = build()

        then:
        true
    }
}
