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

import com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesPlugin
import com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesContextLoader
import com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesExtension
import com.bmuschko.gradle.kubernetes.plugin.domain.CommonFunctions
import com.bmuschko.gradle.kubernetes.plugin.domain.ConfigureAware
import com.bmuschko.gradle.kubernetes.plugin.domain.ResponseAware
import com.bmuschko.gradle.kubernetes.plugin.domain.RetryAware
import com.bmuschko.gradle.kubernetes.plugin.domain.ReactiveStreamsAware

import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.RetryPolicy
import net.jodah.failsafe.function.ContextualCallable

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
 *              // 2.) Configure any passed `config` closures on `foundPods` thus
 *              //     honoring the contract set by the `ConfigAware` trait.
 *              obj objReconfigured = configureOn(objToConfigure)
 *
 *              // 3.) Apply any user-defined inputs AFTER we satisfy `ConfigAware` contract.
 *              def objWithUserInputs = applyInputs(objReconfigured)
 *
 *              // 4.) Do some work with the `foundPods` object
 *              def localResponse = objWithUserInputs.list()
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
abstract class AbstractKubernetesTask extends DefaultTask implements ConfigureAware, ResponseAware, ReactiveStreamsAware, RetryAware, CommonFunctions {

    @Internal
    private static final RetryPolicy DEFAULT_RETRY_POLICY = RetryPolicy.NEVER

    @Internal
    GradleKubernetesContextLoader contextLoader

    @TaskAction
    void start() {
        boolean executionFailed = false
        try {

            // 1.) Honor the reactive-streams `onNext` callback.
            handleOnNext()

        } catch (final Exception possibleException) {

            // 2.) Honor the reactive-streams `onError` callback.
            executionFailed = true
            handleOnError(possibleException)
        }

        // 3.) Honor the reactive-streams `onError` callback.
        if(!executionFailed) {
            handleOnComplete()
        }
    }

    /**
     *  Internal method for handling the `onNext` contract.
     */
    protected handleOnNext() {

        // 1.) Honor the `RetryAware` policy first checking if one has been defined on the extension point,
        //     and if not valid then check this task if one has been defined, and then as a last resort
        //     fallback to the default which is akin to normal execution (i.e. no retries).
        final GradleKubernetesExtension extension = project.extensions.getByName(GradleKubernetesPlugin.EXTENSION_NAME)
        final RetryPolicy retryPolicy = retry() ?: (extension.retry() ?: DEFAULT_RETRY_POLICY)

        // 2.) Execute the overridden `handleClient` method on our custom
        //     `KubernetesClient` classpath.
        final Closure executionClosure = { contextLoader.withClasspath { handleClient(it) } }
        def executionResponse = Failsafe.with(retryPolicy).get(executionClosure as ContextualCallable)

        // 3.) Honor the reactive-stream `onNext` callback but ONLY if we have a
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
    }

    /**
     *  Internal method for handling the `onError` contract.
     */
    protected handleOnError(final Exception possibleException) {
        if (onError()) {
            onError().call(possibleException)
        } else {
            throw possibleException
        }
    }

    /**
     *  Internal method for handling the `onComplete` contract.
     */
    protected handleOnComplete() {
        if (onComplete()) {
            onComplete().call()
        }
    }

    /**
     *  Pass the fully created `kubernetes-client` to the implementing
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
     * These inputs/properties MUST be applied AFTER the contract
     * set by `ConfigAware` and thus after calling `configureOn`.
     *
     * If no inputs are defined then calling this super-class
     * version, which just returns the Object passed in, is OK.
     *
     * If inputs are defined then the downstream task must override
     * this method, apply any user-defined inputs, and return
     * the potentially newly created Object (this happens more times
     * than naught as the `kubernetes-client` uses fluents/builders for
     * everything).
     */
    def applyInputs(objectToApplyInputsOn) {
        objectToApplyInputsOn
    }
}
