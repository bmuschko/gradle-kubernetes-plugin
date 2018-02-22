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
package com.bmuschko.gradle.kubernetes.plugin.tasks.system

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask

/**
 * Get, and print minimally, the system Configuration.
 */
class Configuration extends AbstractKubernetesTask {

    @Override
    def handleClient(kubernetesClient) {

        def objToConfigure = kubernetesClient.getConfiguration()
        def objReconfigured = configureOn(objToConfigure)

        logger.quiet "Api-Version: ${objReconfigured.getApiVersion()}"
        logger.quiet "Master-URL: ${objReconfigured.getMasterUrl()}"

        // register response for downstream use and return list of items
        // for `onNext` execution.
        responseOn(objReconfigured)
    }
}

