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
import com.bmuschko.gradle.kubernetes.plugin.domain.ConfigureAware
import com.bmuschko.gradle.kubernetes.plugin.domain.ResponseAware
import com.bmuschko.gradle.kubernetes.plugin.domain.ReactiveStreamsAware

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal

/*
 *  Base abstract task that all tasks of this plugin are required to extend.
 *
 *  Downstream implementations need to take careful care of the traits implemeted
 *  here and how they work with custom tasks. A typical example might look like:
 *
 *      class SomeKubernetesTask extends AbstractKubernetesTask {
 *
 *          @Override
 *          def handleClient(kubernetesClient) {
 *
 *              // 1.) Used passed `kubernetesClient` to get `pods` object.
 *              def objToConfigure = kubernetesClient.pods()
 *
 *              // 2.) Apply any user-defined inputs first before we configure
 *              def objWithUserInputs = applyUserDefinedInputs(objToConfigure)
 *
 *              // 3.) Configure any passed `config` closures on `foundPods` thus
 *              //     honoring the contract set by the `ConfigAware` trait.
 *              obj objReconfigured = configureOn(objWithUserInputs)
 *
 *              // 4.) Do some work with the `foundPods` object
 *              def localResponse = objReconfigured.list()
 *
 *              // 5.) Register a response object for downstream use thus
 *              //     honoring the contact set by the `ResponseAware` trait.
 *              //
 *              //     Furthermore, and because `response(object)` returns the
 *              //     passed object, we can honor the reactive-stream `onNext`
 *              //     contract by returning an arbitrary object which, at least
 *              //     in this case, is a list of items via the `getItems()`
 *              //     method.
 *              responseOn(localResponse).getItems()
 *          }
 *      }
 *
 *
 */
abstract class AbstractKubernetesTask extends DefaultTask implements ConfigureAware, ResponseAware, ReactiveStreamsAware {

    @Internal
    GradleKubernetesContextLoader contextLoader

    @TaskAction
    void start() {
        boolean executionFailed = false
        try {

            // 1.) Execute the overridden `handleClient` method on our custom
            //     `KubernetesClient` classpath.
            def executionResponse = contextLoader.withClasspath { handleClient(it) }

            // 2.) Honor the reactive-stream `onNext` callback if we have a
            //     non-null response.
            if (executionResponse && onNext()) {
                if (executionResponse instanceof Collection || executionResponse instanceof Object[]) {
                    for (def responseIteration : executionResponse) {
                        onNext().call(responseIteration)
                    }
                } else {
                    onNext().call(executionResponse)
                }
            }
        } catch (final Exception possibleException) {

            // 3.) Honor the reactive-stream `onError` callback, if applicable,
            //     with the just thrown Exception.
            executionFailed = true
            if (onError()) {
                onError().call(possibleException)
            } else {
                throw possibleException
            }
        }

        // 4.) Honor the reactive-stream `onComplete` callback if applicable.
        if(!executionFailed && onComplete()) {
            onComplete().call()
        }
    }

    /**
     *  Pass the fully created `KubernetesClient` to the implementing
     *  class to do some work with.
     *
     *  Optionally return a valid Object to pass to super-class
     *  invocation of `onNext` reactive-stream. If it doesn't
     *  make sense to return something than returning a `null`
     *  will suffice.
     */
    abstract def handleClient(kubernetesClient)

    /**
     * Single method where tasks will apply any user-defined inputs.
     * These inputs/properties MUST be applied BEFORE the `configureOn`
     * method is called as that is the contract we are creating.
     * 
     * If no inputs are defined then calling this super-class
     * version, which just returns the Object, is OK.
     * 
     * If inputs are defined then the downstream task must override
     * this method, apply any user-defined inputs, and return
     * the potentially newly created Object (this happens more times
     * than naught as the `kubernetes-client` uses builders for
     * everything).
     */
    def applyUserDefinedInputs(objectToApplyInputsOn) {
        objectToApplyInputsOn 
    }
}
