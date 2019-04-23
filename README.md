# gradle-bazel-plugin

## Getting started

Plugin provides gradle tasks to work with [`bazel`](https://www.bazel.build) workspaces.

Loading and applying the plugin:
```groovy
plugins {
    id "org.jetbrains.bazel"
}

apply plugin: 'org.jetbrains.bazel'

import org.jetbrains.bazel.*
```
**Note:** you may want to set up bazel dependant environment prior to execution of gradle or adjust environment variables in script.

## Global Configuration
Global configuration allows you to specify options that will be inherited by bazel tasks:
* [executable](#executable)
* [startupOptions](#startup-options)
* [commandOptions](#command-options)
* [environment](#environment)

Refer to corresponding task options for details on format.

#### Example
```groovy
bazel {
    startupOptions = [
        home_rc: false,
        max_idle_secs: 21600,
    ]
    commandOptions = [
        compilation_mode: 'opt',
        cxxopt: [
            '-fno-exceptions',
            '-ffloat-store',
        ],
    ]
}
```

## Tasks
Plugin introduces multiple tasks that wrap specific `bazel` commands underneath:

| Task             | Bazel Command                                                                         | Extra Parameters                  |
|------------------|---------------------------------------------------------------------------------------|-----------------------------------|
| `BazelBuildTask` | [`build`](https://docs.bazel.build/versions/master/command-line-reference.html#build) | *targets*, e.g. `['a', 'b', 'c']` |
| `BazelCleanTask` | [`clean`](https://docs.bazel.build/versions/master/command-line-reference.html#clean) | none                              |
| `BazelTestTask`  | [`test`](https://docs.bazel.build/versions/master/command-line-reference.html#test)   | *targets*, e.g. `['a', 'b', 'c']` |
| `BazelRunTask`   | [`run`](https://docs.bazel.build/versions/master/command-line-reference.html#run)     | *target*, e.g. `'a'`              |

Tasks have common set of paramters:

| Parameter                                      | Mandatory | Type                                       | Description                                               |
|------------------------------------------------|-----------|--------------------------------------------|-----------------------------------------------------------|
| <a name="executable"></a>*executable*          | no        | String                                     | Full path to `bazel` executable or name to search in PATH |
| <a name="workspace"></a>*workspace*            | yes       | File                                       | `bazel` workspace (containing WORKSPACE file)             |
| <a name="sturtup-options"></a>*sturtupOptions* | no        | Map\<String, [**Value**\*](#option-value)> | Task specific `bazel` startup options                     |
| <a name="command-ptions"></a>*commandOptions*  | no        | Map\<String, [**Value**\*](#option-value)> | Task specific `bazel` command options                     |
| <a name="environment"></a>*environment*        | no        | Map<String,String>                         | Variables to be added to environment for this task        |

<a name="option-value"></a>\***Value** may be *Integer*, *Boolean*, *String*, *List<String>* depending on original bazel  command line option type:

1. Boolean and tristate options:
    ```
    --option1
    --nooption2
    ```
    become:
    ```groovy
    sturtupOptions = [
        ...
        option1: true,
        option2: false,
        ...
    ]
    ```
    **Global option inheritance strategy:** overrided by task option

2. Single value options:
    ```
    --option1=value
    --option2=42
    ```
    become:
    ```groovy
    sturtupOptions = [
        ...
        option1: 'value',
        option2: 42,
        ...
    ]
    ```
    **Global option inheritance strategy:** overrided by task option

3. Multiple use options:
    ```
    --option=value1
    --option=value2
    --option=value3
    ```
    become:
    ```groovy
    sturtupOptions = [
        ...
        option: [
            'value1',
            'value2',
            'value2',
        ],
        ...
    ]
    ```

    **Global option inheritance strategy:** extended by task option

Please refer to [Bazel Command Line Reference](https://docs.bazel.build/versions/master/command-line-reference.html) for full set of options.

#### Example
```groovy
task project_build(type: BazelBuildTask) {
    workspace = file('C:/ws/project')
    startupOptions = [
        ignore_all_rc_files: true,
    ]
    commandOptions = [
        copt: [
            '-DVAR1=VALUE1',
            '-DVAR2=VALUE2',
        ],
        dynamic_mode: 'fully',
    ]
    environment = [
        ENV_VAR1: 'VALUE1',
        ENV_VAR2: 'VALUE2',
    ]
    targets = ['target1', 'target2']
}

task project_clean(type: BazelCleanTask) {
    workspace = file('C:/ws/project')
    commandOptions = [ expunge: true ]
}
```

## License
meow
