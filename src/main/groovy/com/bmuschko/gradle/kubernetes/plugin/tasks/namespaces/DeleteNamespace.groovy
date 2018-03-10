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
import org.gradle.api.tasks.Optional

/**
 * Delete a namespace.
 */
class DeleteNamespace extends AbstractKubernetesTask {

    @Input
    @Optional
    String namespace

    @Input
    @Optional
    Long gracePeriod

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Deleting namespace...'
        def objToConfigure = kubernetesClient.namespaces()

        // configure on namespace
        def objReconfigured = configureOn(objToConfigure)
        
        // apply user-defined inputs
        def objWithUserInputs = applyInputs(objReconfigured)

        // invoke method to delete
        def localResponse = objWithUserInputs.delete()
        if (!localResponse) {
            throw new GradleException("Failed deleting namespace")
        }
          
        // register response for downstream use which in this case
        // is just a `Boolean` instance.
        responseOn(localResponse)
    }

    @Override
    def applyInputs(objectToApplyInputsOn) {
        def objWithInputs = objectToApplyInputsOn

        if (namespace) {
            objWithInputs = objWithInputs.withName(namespace)
        }
        
        if (gracePeriod) {
            objWithInputs = objWithInputs.withGracePeriod(gracePeriod)
        }

        objWithInputs
    }
}

