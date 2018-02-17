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

import com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesContextLoader
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

/*
 * Responsible for passing along a fully created and loaded `KubernetesClient` to the
 * downstream task that extends this class.
 */
abstract class AbstractKubernetesTask extends AbstractReactiveStreamsTask {

    @Internal
    GradleKubernetesContextLoader contextLoader

    /**
     * Possibly null response object returned from the execution.
     */
    @Internal
    private def response

    @Override
    def runReactiveStream() {
        def response
        runInKubernetesClassPath { kubernetesClient ->
            response = runRemoteCommand(kubernetesClient)
        }
        response
    }

    void runInKubernetesClassPath(final Closure closure) {
        contextLoader.withClasspath(closure)
    }

    /**
     *  Optionally return a valid Object to pass to super-class
     *  invocation of `onNext` reactive-stream. If it doesn't
     *  make sense to return something than returning a `null`
     *  will suffice.
     */
    abstract def runRemoteCommand(kubernetesClient)

    /**
     *  Response, possibly null, returned from execution. This is an attempt
     *  at creating a generic way all tasks of this plugin return data. The
     *  data itself can be anything and is not restricted by any rules imposed
     *  by our super-class `AbstractReactiveStreamsTask`. This CAN be the data
     *  returned from `runRemoteCommand` but does not necessarily have to be.
     *
     *  Internal tasks should take careful care to invoke the `registerResponse(def)`
     *  method, generally as the last line of execution, to give external downstream tasks
     *  (i.e. tasks/code not from this plugin) something to work with.
     */
    def response() {
        response
    }

    /**
     *  Internal helper method for all tasks of this plugin to explicitly
     *  register a response object for external downstream use.
     */
    protected def registerResponse(final def responseToRegister) {
        this.response = responseToRegister
    }
}
