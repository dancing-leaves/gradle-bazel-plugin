package org.jetbrains.bazel

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

class BazelPluginExtension extends BazelExecutorConfiguration {
    static final def EXTENSION_NAME = 'bazel'

    @Inject
    BazelPluginExtension(ObjectFactory objects) {
        executable = objects.property(String)
        options = objects.mapProperty(String, Option)
        command = objects.property(String)
        arguments = objects.mapProperty(String, Option)
    }


}