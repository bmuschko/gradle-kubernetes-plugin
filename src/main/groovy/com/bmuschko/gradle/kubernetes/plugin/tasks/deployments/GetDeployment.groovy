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

package com.bmuschko.gradle.kubernetes.plugin.tasks.deployments

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Get a deployment.
 */
class GetDeployment extends AbstractKubernetesTask {

    @Input
    @Optional
    String namespace // which namespace the deployment exists within.

    @Input
    @Optional
    String deployment // name of the deployment to retrieve.

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Getting deployment...'
        def objToConfigure = kubernetesClient.extensions().deployments()

        // no real options to supply so should amount to a no-op
        def objReconfigured = configureOn(objToConfigure)

        // apply user-defined inputs
        def objWithUserInputs = applyInputs(objReconfigured)

        // get the service
        def localResponse = objWithUserInputs.fromServer().get()
        if (!localResponse) {
            throw new GradleException('Deployment could not be found.')
        }

        // register response for downstream use which in this case
        // is just a `Service` instance.
        responseOn(localResponse)
    }

    @Override
    def applyInputs(obj) {
        def objRef = wrapAtomic(obj)
        invokeMethod(objRef, 'inNamespace', namespace)
        invokeMethod(objRef, 'withName', deployment).get()
    }
}
