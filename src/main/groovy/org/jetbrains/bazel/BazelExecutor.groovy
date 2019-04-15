package org.jetbrains.bazel

class BazelExecutor {
    BazelExecutorConfiguration executorConfiguration

    BazelExecutor(executorConfiguration) {
        this.executorConfiguration = executorConfiguration
    }

    def exec() {
        def processBuilder = executorConfiguration.processBuilder
        def process = processBuilder.start()
        process.waitFor()
    }
}
