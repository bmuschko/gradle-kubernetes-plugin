/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmuschko.gradle.kubernetes.plugin.utils

/**
 *  Trait that provides a generic `reactive-streams` API.
 *
 *  The idea is that calling code of a given task has the option
 *  to take advantage of any or all of these closures but is not
 *  bound by any contract to do so. An example might look like:
 *
 *      task listAllNamespaces(type: ListNamespaces) {
 *
 *          // runs for each response object returned. If object
 *          // is a "list" type than this closfure will be invoked for
 *          // each element otherwise the response itself is passed.
 *          onNext { iter ->
 *              logger.quiet "Found namespace: ${iter.name()}"
 *          }
 *
 *          // runs only if execution is successful.
 *          onComplete {
 *              logger.quiet "Task has finished execution"
 *          }
 *
 *          // runs only if exception is thrown.
 *          onError { exc ->
 *              logger.quiet "Exception message: ${exc.message()}"
 *          }
 *      }
 */
trait ReactiveStreamsAware {

    /**
     *  Closure to handle, possibly iterative, results.
     *
     *  If the response returned is of type `Collection` or `Object[]`
     *  then the implementing class should invoke the `onNext` closure
     *  with element of the list.
     *
     *  If the response returned is a POJO than that alone should
     *  be passed to the `onNext` closure.
     */
    private Closure onNext
    Closure onNext() { onNext }
    void onNext(final Closure delegate) {
        this.onNext = delegate
    }

    /**
     *  Closure to handle task completion.
     *
     *  If execution is successful the `onComplete` closure will be called
     *  with no parameters. This is analagous to a `doLast` closure with
     *  the difference being is that this executes first.
     */
    private Closure onComplete
    Closure onComplete() { onComplete }
    void onComplete(final Closure delegate) {
        this.onComplete = delegate
    }

    /**
     *  Closure to handle an exception.
     *
     *  If execution throws an exception the `onError` closure will be called
     *  passing in the exception object itself as a single parameter. It is up
     *  to the calling code to do something with this exception otherwise task
     *  execution will succeed.
     */
    private Closure onError
    Closure onError() { onError }
    void onError(final Closure delegate) {
        this.onError = delegate
    }
}
