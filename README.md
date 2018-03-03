# WIP: if you would like to help out with the initial development to get this project off the ground please open an ISSUE to ask about what needs to be done or what you would like to work on.

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

- **Name**: name of the gradle task
- **`config{}`**: Object the `config{}` closure maps to and is further documented below.
- **`onNext{}`**: Object the next iteration of `onNext{}` will receive and is further documented below.
- **`response()`**: Object the `response()` method returns AFTER execution of a given task has completed.

### Namespace operations

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [ListNamespaces](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/ListNamespaces.groovy) | [NonNamespaceOperation](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/dsl/NonNamespaceOperation.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) | [NamespaceList](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/NamespaceList.html) |
| [CreateNamespace](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/CreateNamespace.groovy) | [MetadataNestedImpl](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/NamespaceFluentImpl.MetadataNestedImpl.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) |
[GetNamespace](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/GetNamespace.groovy) | [N/A]() | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) |
| [DeleteNamespace](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/DeleteNamespace.groovy) | [MetadataNestedImpl](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/NamespaceFluentImpl.MetadataNestedImpl.html) | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) |

### System operations

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [Configuration](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/system/Configuration.groovy) | [N/A]() | [Configuration](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/Config.html) | [Configuration](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/Config.html) |


## On `config{}`

All Objects (e.g. Tasks, Extensions, etc) implement the [ConfigAware](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/domain/ConfigureAware.groovy) trait. As such the end-user can take advantage of the `config{}` closure to further configure said objects. The respective `config{}` closure maps to the backing/documented object. A typical use-case would be to configure a task like so:
```
task myCustomNameSpace(type: CreateNamespace) {
    config {
        withName("hello-world")
    }
}
```
The `config{}` closure has its delegate (as well as the first parameter) set to the object you're allowed to configure within a given context. In the example above, and documented in the table below, the `CreateNamespace` task allows you to configure the `MetadataNestedImpl` object which really can just be thought of as a super-class to the internal `Namespace` instance. In java, and using the same `kubernetes-client`, this would look something like:
```
NonNamespaceOperation<Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>> namespaces = client.namespaces();
Resource<Namespace, DoneableNamespace> withName = namespaces.withName("hello-world");
```
The `config{}` closure is an attempt at trying to provide a common means of configuring Objects in a very gradle like fashion.

## On `response()`

All tasks implement the [ResponseAware](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/domain/ResponseAware.groovy) trait. As such the end-user, and ONLY upon completion of a given task, will be able to query for a given tasks `response()` object. Furthermore the Object returned is different for each task and is documented in the table below.

As each task does some work in the backend it's sometimes helpful, or even desired, to get the returned object for further downstream inspection. Suppose you wanted a programmatic way of getting the name of the namespace you just created. You could do something like:
```
task myCustomNameSpace(type: CreateNamespace) {
    config { specialConfig ->
        withName("hello-world")
    }
}

task downstreamTask(dependsOn: myCustomNameSpace) {
    doLast {
        def foundName = myCustomNameSpace.response().getMetadata().getName()
        // now do something with the `foundName` String
    }
}
```
Much like the `config{}` closure the `response()` method is an attempt at providing a standard way across all tasks of accessing the returned Object from the internal `kubernetes-client` invocation.

## Reactive-Streams

[reactive-streams](https://github.com/reactive-streams/reactive-streams-jvm) support is an optional feature you can take advantage of and works for all tasks. We try to align with best practices but given that we are executing within a gradle context we break the expected API from time to time to keep the look and feel of our plugin. Each task generally behaves the same but if one doesn't please visit have a look at the task definition itself for any documentaiton or nuance surrounding its use.

Documentation on how we implement this feature can be found in our [HERE](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/AbstractReactiveStreamsTask.groovy).
Examples to help you get started can be found [HERE]().

### onError

The `onError` closure is passed the exception that is thrown for YOU to handle. If you silently ignore we will not throw the exception behind the scenes.
The below example is a common use-case that arises when someone wants to remove a container whether it exists or not but does not want to fail hard.

```
import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

task listNamespaces(type: ListNamespaces) {
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

task listNamespaces(type: ListNamespaces) {
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

task listNamespaces(type: ListNamespaces) {
    onComplete {
        logger.quiet 'Executes first'
    }
    doLast {
        logger.quiet 'Executes second'
    }
}
```


## Examples

The [functionalTests](https://github.com/bmuschko/gradle-kubernetes-plugin/tree/master/src/functionalTest/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks) provide many examples that you can use for inspiration within your own code. If there are any questions about how to use a given feature feel free to open an issue and just ask.

## Additional Resources
* [Kubernetes Client](https://github.com/fabric8io/kubernetes-client)
* [Kubernetes Setup](https://kubernetes.io/docs/setup/pick-right-solution/)
* [Release Process](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/docs/RELEASE_PROCESS.md)
