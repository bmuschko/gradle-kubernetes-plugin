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

package com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input

/**
 * Get a namespace.
 */
class GetNamespace extends AbstractKubernetesTask {

    @Input
    Closure<String> namespace

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Getting namespace...'
        def objToConfigure = kubernetesClient.namespaces()

        // apply user-defined inputs
        def objWithUserInputs = applyUserDefinedInputs(objToConfigure)
        
        // no real options so supply so should amount to a no-op
        def objReconfigured = configureOn(objWithUserInputs)

        // get the namespace
        def localResponse = objReconfigured.fromServer().get()
        if (!localResponse) {
            throw new GradleException("Namespace '" + namespace.call() + "' could not be found.")
        }
                   
        // register response for downstream use which in this case
        // is just a `Namespace` instance.
        responseOn(localResponse)
    }
    
    def applyUserDefinedInputs(objectToApplyInputsOn) {
        objectToApplyInputsOn.withName(namespace.call())
    }
}

