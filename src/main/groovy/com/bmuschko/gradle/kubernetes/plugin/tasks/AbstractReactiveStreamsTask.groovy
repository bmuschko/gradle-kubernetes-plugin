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
package com.bmuschko.gradle.kubernetes.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Provides reactive-streams execution for tasks of this plugin.
 */
abstract class AbstractReactiveStreamsTask extends DefaultTask {

    /**
     * Closure to handle the possibly thrown exception.
     */
    @Optional
    Closure onError

    /**
     * Closure to handle results.
     */
    @Optional
    Closure onNext

    /**
     * Closure to handle task completion.
     */
    @Optional
    Closure onComplete

    @TaskAction
    void start() {
        boolean commandFailed = false
        try {
            final def executionResponse = runReactiveStream()
            if (executionResponse && onNext) {
                if (executionResponse instanceof Collection || executionResponse instanceof Object[]) {
                    for (def responseIteration : executionResponse) {
                        onNext.call(responseIteration)
                    }
                } else {
                    onNext.call(executionResponse)
                }
            }
        } catch (Exception possibleException) {
            commandFailed = true
            if (onError) {
                onError(possibleException)
            } else {
                throw possibleException
            }
        }
        if(!commandFailed && onComplete) {
            onComplete()
        }
    }

    /**
     *  Optionally return a valid Object to pass to `onNext`
     *  reactive-stream. If it doesn't make sense to return
     *  something than returning a `null` will suffice.
     *
     *  If the returned object is of type `Collection` or `Object[]`
     *  then we'll pass each instance in the list to its own `onNext`
     *  invocation. If however the returned object is NOT a collection
     *  of some sort than we will pass it as-is, but only if it's non-null,
     *  to a single invocation of `onNext`.
     */
    abstract def runReactiveStream()
}
