# gradle-kubernetes-plugin

Gradle plugin for working with Kubernetes.

## Project(s) Status

| CI | Codecov | Docs | Questions | Release |
| :---: | :---: | :---: | :---: | :---: |
| [![Build Status](https://travis-ci.org/bmuschko/gradle-kubernetes-plugin.svg?branch=master)](https://travis-ci.org/bmuschko/gradle-kubernetes-plugin) | [![codecov](https://codecov.io/gh/bmuschko/gradle-kubernetes-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/bmuschko/gradle-kubernetes-plugin) | [![Docs](https://img.shields.io/badge/docs-latest-blue.svg)](http://htmlpreview.github.io/?https://github.com/bmuschko/gradle-kubernetes-plugin/blob/gh-pages/docs/index.html) | [![Stack Overflow](https://img.shields.io/badge/stack-overflow-4183C4.svg)](https://stackoverflow.com/questions/tagged/gradle-kubernetes-plugin) | [![gradle-kubernetes-plugin](https://api.bintray.com/packages/bmuschko/gradle-plugins/gradle-kubernetes-plugin/images/download.svg) ](https://bintray.com/bmuschko/gradle-plugins/gradle-kubernetes-plugin/_latestVersion) |

## How to Setup

```
buildscript() {
    repositories {
        jcenter()
    }
    dependencies {
        classpath group: 'com.bmuschko', name: 'gradle-kubernetes-plugin', version: 'X.Y.Z'
    }
 }

 apply plugin: 'gradle-kubernetes-plugin'
 ```

## How to Configure

The `kubernetes` extension acts as a mapper to the [Config](https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/Config.java) object provided by the [kubernetes-client](https://github.com/fabric8io/kubernetes-client) library which we use in the backend. This allows you to configure this plugin in exactly the same way you would configure the java client.

```
 kubernetes {
    config {
        withMasterUrl("https://mymaster.com")
    }
 }
```

All [additional options](https://github.com/fabric8io/kubernetes-client#configuring-the-client) that exist to configure the client are also honored here.

## Tasks
### Namespace operations

| Name | Description |
| --- | --- |
| [ListNamespaces](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/ListNamespaces.groovy) | List available namespaces |

### System operations

| Name | Description |
| --- | --- |
| [Configuration](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/system/Configuration.groovy) | Get the system configuration |

## Reactive-Streams

[reactive-streams](https://github.com/reactive-streams/reactive-streams-jvm) support is an optional feature you can take advantage of and works for all tasks. We try to align with best practices but given that we are executing within a gradle context we break the expected API from time to time to keep the look and feel of our plugin. Each task generally behaves the same but if one doesn't please visit have a look at the task definition itself for any documentaiton or nuance surrounding its use.

Documentation on how we implement this feature can be found in our [HERE](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/AbstractReactiveStreamsTask.groovy).
Examples to help you get started can be found [HERE]().

### onError

The `onError` closure is passed the exception that is thrown for YOU to handle. If you silently ignore we will not throw the exception behind the scenes.
The below example is a common use-case that arises when someone wants to remove a container whether it exists or not but does not want to fail hard.

```
import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

task listAllNamespaces(type: ListNamespaces) {
    onError { exception ->
        if (exception.message.contains('something I care about'))
            throw exception
    }
}
```

### onNext

The `onNext` closure is passed the next iterative response upon execution or if the response contains a list then the next item in that list. For all other tasks we simply hand you back the object that is given to us by the execution. Thus, and much like the `onException` closure, all delegation is now in your control. Any properties/values expected to be set will not be done unless YOU do them.

Iterative tasks are things like `ListNamespaces`. These tasks have output which can be iterated over or return a list (e.g.` Collection` or `Object[]`) of some sort.

Suppose we want to list out, or simply work with, each available namespace. We might do something like:
```
import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

task listAllNamespaces(type: ListNamespaces) {
    onNext { namespace ->
        logger.quiet "Found namespace: ${namespace.name()}"
    }
}
```
If `4` namespaces were present then the above `onNext` closure would execute for each found.

### onComplete

The `onComplete` closure is not passed anything upon execution. It works in the same fashion that `doLast` does but is instead part of this task and thus executes before `doLast` does. This closure executes ONLY upon success. The below example demonstrates how this works.

```
import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

task listAllNamespaces(type: ListNamespaces) {
    onComplete {
        logger.quiet 'Executes first'
    }
    doLast {
        logger.quiet 'Executes second'
    }
}
```

## Additional Resources
* [Kubernetes Client](https://github.com/fabric8io/kubernetes-client)
* [Kubernetes Setup](https://kubernetes.io/docs/setup/pick-right-solution/)
* [Release Process](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/docs/RELEASE_PROCESS.md)
