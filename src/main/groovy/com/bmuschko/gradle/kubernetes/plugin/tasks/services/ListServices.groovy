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

package com.bmuschko.gradle.kubernetes.plugin.tasks.services

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * List available Services.
 */
class ListServices extends AbstractKubernetesTask {

    @Input
    @Optional
    Map<String, String> withLabels

    @Input
    @Optional
    Map<String, String> withoutLabels

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Listing services...'
        def objToConfigure = kubernetesClient.services()

        // configure on the services instance itself
        def objReconfigured = configureOn(objToConfigure)
        
        // apply user-defined inputs
        def objWithUserInputs = applyInputs(objReconfigured)

        // get the `ServiceList` object which in itself is NOT a list.
        def localResponse = objWithUserInputs.list()

        // register response for downstream use and return list of items
        // for `onNext` execution. The `getItems()` call will return null
        // if no items were found.
        responseOn(localResponse).getItems()
    }

    @Override
    def applyInputs(obj) {
        def objRef = wrapAtomic(obj)
        invokeMethod(objRef, 'withLabels', withLabels)
        invokeMethod(objRef, 'withoutLabels', withoutLabels).get()
    }
}

