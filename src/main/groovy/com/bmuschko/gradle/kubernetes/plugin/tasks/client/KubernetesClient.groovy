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
package com.bmuschko.gradle.kubernetes.plugin.tasks.client

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask

/**
 * Get the `kubernetes-client` instance.
 * 
 * This is for advanced usage and is NOT advised for day to day developers
 * to play around with as the instance handed to you is shared by all tasks
 * and anything done to it can potentially effect everything else.
 */
class KubernetesClient extends AbstractKubernetesTask {

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Getting kubernetes-client...'
        def objToConfigure = kubernetesClient

        // configure on instance
        def objReconfigured = configureOn(objToConfigure)
        
        // apply user-defined inputs
        def objWithUserInputs = applyInputs(objReconfigured)
                   
        // register response for downstream use which in this case
        // is just a `kubernetes-client` instance.
        responseOn(objWithUserInputs)
    }
}

