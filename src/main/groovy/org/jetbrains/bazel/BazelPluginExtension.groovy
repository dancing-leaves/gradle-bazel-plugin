package org.jetbrains.bazel

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

class BazelPluginExtension extends BazelExecutorConfiguration {
    static final def EXTENSION_NAME = 'bazel'

    final Property<String> executable

    final MapProperty<String,Object> startupOptions

    final MapProperty<String,Object> commandOptions

    final MapProperty<String,String> environment

    @Inject
    BazelPluginExtension(ObjectFactory objects) {
        executable = objects.property(String)
        startupOptions = objects.mapProperty(String, Object)
        command = objects.property(String)
        commandOptions = objects.mapProperty(String, Object)
    }
}
