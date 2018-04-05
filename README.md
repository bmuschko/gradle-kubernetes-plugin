# WIP: if you would like to help out with the initial development to get this project off the ground please open an ISSUE to ask about what needs to be done or what you would like to work on.

# gradle-kubernetes-plugin

Gradle plugin for working with Kubernetes.

## Status

| CI | Codecov | Docs | Questions | Release |
| :---: | :---: | :---: | :---: | :---: |
| [![Build Status](https://travis-ci.org/bmuschko/gradle-kubernetes-plugin.svg?branch=master)](https://travis-ci.org/bmuschko/gradle-kubernetes-plugin) | [![codecov](https://codecov.io/gh/bmuschko/gradle-kubernetes-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/bmuschko/gradle-kubernetes-plugin) | [![Docs](https://img.shields.io/badge/docs-latest-blue.svg)](http://htmlpreview.github.io/?https://github.com/bmuschko/gradle-kubernetes-plugin/blob/gh-pages/docs/index.html) | [![Stack Overflow](https://img.shields.io/badge/stack-overflow-4183C4.svg)](https://stackoverflow.com/questions/tagged/gradle-kubernetes-plugin) | [![gradle-kubernetes-plugin](https://api.bintray.com/packages/bmuschko/gradle-plugins/gradle-kubernetes-plugin/images/download.svg) ](https://bintray.com/bmuschko/gradle-plugins/gradle-kubernetes-plugin/_latestVersion) |

## Design Goals

Learning from, and building upon, the work done, lessons learned, and features requested, with the [gradle-docker-plugin]() we sought to create a plugin that was easy to use up front but with the proper hooks/constructs in place to allow for more flexible solutions and complicated scenarios should the developer want to take advantage of them. Things like (but not limited to):
- CRUD operations around all kubernetes endpoints (e.g. namespaces, services, pods, etc).
- Dependent libraries loaded into their own class-loader so as not to clobber `buildscript` classpath.
- [config{}](#on-config) provides a common means of configuring the backing object Tasks are based upon. Instead of exposing every possible property the backing `kubernetes-client` may provide we instead expose the most common ones and let the user further configure things through this construct should the need arise.
- [retry{}](#on-retry) provides a common means of configuring retries for a given Task. The construct itself can be provided globally on the extension point, for all tasks to inherit, or upon each individual task for more granular use-cases.
- [response()](#on-response) hands back to the user, once task execution has finished, the object given to us by the `kubernetes-client` execution. This allows downstream tasks to query a previously ran Task for its output and potentially design more complicated scenarios with it.
- [reactive-streams](#on-reactive-streams) gives users a more dynamic experience when working with a give tasks life-cycle.
- More streamlined, simplifed, and documented codebase allowing for easier contributions from the community.

## Getting Started

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

## The `kubernetes` extension point

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

The below table(s) document each of our tasks and their respective `features` in depth details of which are provided further below. Special care should be taken when using the `config{}` construct as it's considered an **ADVANCED** feature and developers should favor using the OOTB inputs/properties of the task itself whenever possible vs configuring things directly on the backing `kubernetes-client` object.

**key table**

| Column | Description |
| --- | --- |
| Name | Name and hyperlink to Task source. |
| `config{}` | Object `config{}` closure maps to. |
| `onNext{}` | Object next iteration of `onNext{}` closure will receive. |
| `response()` | Object `response()` method returns AFTER task execution has finished. |

### Client operation **_ADVANCED USAGE and doesn't follow Task design patterns_

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [KubernetesClient](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/client/KubernetesClient.groovy) | [KubernetesClient](https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/KubernetesClient.java) | [KubernetesClient](https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/KubernetesClient.java) | [KubernetesClient](https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/KubernetesClient.java) |

### Deployment operations

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [ListDeployments](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/ListNamespaces.groovy) | [MixedOperation](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/dsl/MixedOperation.html) | [Deployment](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/extensions/Deployment.html) | [DeploymentList](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/extensions/DeploymentList.html) |

### Namespace operations

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [ListNamespaces](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/ListNamespaces.groovy) | [NonNamespaceOperation](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/dsl/NonNamespaceOperation.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) | [NamespaceList](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/NamespaceList.html) |
| [CreateNamespace](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/CreateNamespace.groovy) | [MetadataNestedImpl](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/NamespaceFluentImpl.MetadataNestedImpl.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) |
[GetNamespace](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/GetNamespace.groovy) | [N/A]() | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) | [Namespace](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Namespace.html) |
| [DeleteNamespace](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/namespaces/DeleteNamespace.groovy) | [N/A]() | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) |

### Pod operations

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [ListPods](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/pods/ListPods.groovy) | [MixedOperation](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/dsl/MixedOperation.html) | [Pod](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Pod.html) | [PodList](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/PodList.html) |
[CreatePod](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/pods/CreatePod.groovy) | [DoneablePod](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/DoneablePod.html) | [Pod](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Pod.html) | [Pod](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Pod.html) |
[GetPod](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/pods/GetPod.groovy) | [N/A]() | [Pod](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Pod.html) | [Pod](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Pod.html) |
[DeletePod](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/pods/DeletePod.groovy) | [N/A]() | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) |

### Service operations

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [ListServices](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/services/ListServices.groovy) | [MixedOperation](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/dsl/MixedOperation.html) | [Service](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Service.htmll) | [ServiceList](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/ServiceList.html) |
[CreateService](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/services/CreateService.groovy) | [DoneableService](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/DoneableService.html) | [Service](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Service.htmll) | [Service](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Service.html) |
[GetService](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/services/GetService.groovy) | [N/A]() | [Service](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Service.htmll) | [Service](http://static.javadoc.io/io.fabric8/kubernetes-model/2.0.8/io/fabric8/kubernetes/api/model/Service.html) |
[DeleteService](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/services/DeleteService.groovy) | [N/A]() | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) | [Boolean](https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html) |


### System operations

| Name | `config{}` | `onNext{}` | `response()` |
| --- | --- | --- | --- |
| [Configuration](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/system/Configuration.groovy) | [N/A]() | [Configuration](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/Config.html) | [Configuration](http://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/Config.html) |

## Features

This plugin provides various means of configuring, working with, and accessing properties and values of a given `Task`. Through the use of `config{}`, `response()`, and `reactive-streams`, which are each further documented below, the user is given full access to configure their `Task` however the choose, have full access to the response or output of a given `Task`, and be able to work more closely with the life-cycle of a given task.

### On config

All tasks, as well as the `kubernetes` extension point, implement the [ConfigAware](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/domain/ConfigureAware.groovy) trait. This in turn exposes the `config{}` closure allowing developers to further configure said objects. The respective `config{}` closure maps to the backing/documented object. A typical use-case would be to configure a task like so:
```
task myCustomNameSpace(type: CreateNamespace) {
    config {
        withName("hello-world") // applying name via `config{}` construct instead of property
    }
}
```
The `config{}` closure has its delegate (as well as the first parameter) set to the object you're allowed to configure within a given context. In the example above, and documented in the table below, the `CreateNamespace` task allows you to configure the `MetadataNestedImpl` object which really can just be thought of as a super-class to the internal `Namespace` instance. In java, and using the same `kubernetes-client`, this would look something like:
```
NonNamespaceOperation<Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>> namespaces = client.namespaces();
Resource<Namespace, DoneableNamespace> withName = namespaces.withName("hello-world");
```
The `config{}` closure is an attempt at trying to provide a common means of configuring Objects in a very gradle like fashion. This is considered an **ADVANCED** feature so please only use if the OOTB supplied properties are not enough.

### On retry

All tasks, as well as the `kubernetes` extension point, implement the [RetryAware](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/domain/RetryAware.groovy) trait. This allows the developer to define a common means, via the `retry{}` closure, to configure how tasks should be re-run. Developers can either define the `retry{}` closure on the _extension point_ or on the _task itself_ for more granular configurations. Some typical use-cases are as follows:

When defined on the extension:
```
kubernetes {
   retry {
       withDelay(10, TimeUnit.SECONDS)
       withMaxRetries(3)
   }
}
```
When defined on a task:
```
task getNamespace(type: GetNamespace) {
    namespace = "my-eventually-existing-namespace"
    retry {
        withDelay(10, TimeUnit.SECONDS)
        withMaxRetries(3)
    }
}
```
Task definitions of the `retry{}` closure take precedence over those defined on the extension point.

We use the [failsafe](https://github.com/jhalterman/failsafe) library behind the scenes to execute code internally. Thus when you define/code a `retry{}` closure you're actually configuring a newly created instance of [RetryPolicy](http://jodah.net/failsafe/javadoc/net/jodah/failsafe/RetryPolicy.html).

It should be noted that we are not actually re-running the actual `Task` through some gradle magic done behind the scenes. Instead we are just re-running the internal "execution block" that each task implements.

### On response

All tasks implement the [ResponseAware](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/domain/ResponseAware.groovy) trait. As such the end-user, and ONLY upon completion of a given task, will be able to query for a given tasks `response()` object. Furthermore the Object returned is different for each task and is documented in the table below.

As each task does some work in the backend it's sometimes helpful, or even desired, to get the returned object for further downstream inspection. Suppose you wanted a programmatic way of getting the name of the namespace you just created. You could do something like:
```
task myCustomNameSpace(type: CreateNamespace) {
    config {
        withName("hello-world") // applying name via `config{}` construct instead of property
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

### On reactive-streams

[reactive-streams](https://github.com/reactive-streams/reactive-streams-jvm) support is an optional feature you can take advantage of and works for all tasks. We try to align with best practices but given that we are executing within a gradle context we break the expected API from time to time to keep the look and feel of our plugin. Each task generally behaves the same but if one doesn't please visit have a look at the task definition itself for any documentaiton or nuance surrounding its use.

Documentation on how we implement this feature can be found in our [HERE](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/src/main/groovy/com/bmuschko/gradle/kubernetes/plugin/tasks/AbstractReactiveStreamsTask.groovy).
Examples to help you get started can be found [HERE]().

#### onError stream

The `onError` closure is passed the exception that is thrown for YOU to handle. If you silently ignore we will not throw/re-throw the exception behind the scenes. Suppose you want to automate the creation of a `Namespace` but you don't want to fail if the namespace already exists. In this scenario you could do something like the below:

```
import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.CreateNamespace

task createNamespace(type: CreateNamespace) {
    namespace = "namespace-that-possibly-exists"
    onError { exception ->
        if (exception.message.contains('namespace already exists')) { // not an actual message just an example
            // do nothing
        } else {
            throw execption
        }
    }
}
```

#### onNext stream

The `onNext` closure is passed the next iterative response upon execution or if the response contains a list then the next item in that list. For all other tasks we simply hand back the object that is given to us by the execution.

Iterative tasks are things like `ListNamespaces` or `ListPods`. These tasks have output which can be iterated over or return a list (e.g.` Collection` or `Object[]`) of some sort.

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

#### onComplete stream

The `onComplete` closure is not passed anything upon execution. It works in the same fashion that `doLast` does but is instead part of this task and thus executes before `doLast` kicks. This closure executes ONLY upon success. The below example demonstrates how this works.

```
import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.GetNamespace

task getNamespace(type: GetNamespace) {
    namespace = "my-namespace"
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

## Kubernetes Resources
* [Kubernetes Client](https://github.com/fabric8io/kubernetes-client)
* [Kubernetes Setup](https://kubernetes.io/docs/setup/pick-right-solution/)
* [Reasons Kubernetes deployments fail: Part 1](https://kukulinski.com/10-most-common-reasons-kubernetes-deployments-fail-part-1/)
* [Reasons Kubernetes deployments fail: Part 2](https://kukulinski.com/10-most-common-reasons-kubernetes-deployments-fail-part-2/)
