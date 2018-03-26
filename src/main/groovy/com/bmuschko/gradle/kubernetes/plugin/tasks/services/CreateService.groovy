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

import com.bmuschko.gradle.kubernetes.plugin.domain.PortSpec
import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import org.gradle.api.GradleException
import org.gradle.api.Nullable

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

/**
 * Create a service.
 */
class CreateService extends AbstractKubernetesTask implements PortSpec {

    public static final enum SERVICE_TYPES { ClusterIP, NodePort, LoadBalancer, ExternalName }

    @Input
    @Optional
    String namespace

    @Input
    @Optional
    String service

    @Input
    @Optional
    String type // defaults to SERVICE_TYPES.ClusterIP if not defined

    @Input
    @Optional
    Map<String, String> withLabels

    @Input
    @Optional
    Map<String, String> selector

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Creating service...'
        def objToConfigure = kubernetesClient.services().createNew()

        // configure on meta-data
        def objReconfigured = configureOn(objToConfigure)

        // apply user-defined inputs
        def objWithUserInputs = applyInputs(objReconfigured)

        // finalize creation of service
        def localResponse = objWithUserInputs.done()

        // register response for downstream use which in this case
        // is just a `Service` instance.
        responseOn(localResponse)
    }

    @Override
    def applyInputs(obj) {
        def objRef = wrapAtomic(obj)
        invokeMethod(objRef, 'editOrNewMetadata')
        invokeMethod(objRef, 'withName', service)
        invokeMethod(objRef, 'withNamespace', namespace)
        invokeMethod(objRef, 'withLabels', withLabels)
        invokeMethod(objRef, 'endMetadata')

        invokeMethod(objRef, 'editOrNewSpec')
        if (this.type) {
            invokeMethod(objRef, 'withType', SERVICE_TYPES.valueOf(this.type).toString())
        }
        invokeMethod(objRef, 'addToSelector', this.selector)

        // add requested port specs
        portSpecs.each { portMap ->
            invokeMethod(objRef, 'addNewPort')
            invokeMethod(objRef, 'withName', portMap.name)
            invokeMethod(objRef, 'withProtocol', portMap.protocol)
            invokeMethod(objRef, 'withNodePort', portMap.nodePort)
            invokeMethod(objRef, 'withPort', portMap.podPort)
            invokeMethod(objRef, 'withNewTargetPort', portMap.targetPort)
            invokeMethod(objRef, 'endPort') 
        }

        invokeMethod(objRef, 'endSpec').get()
    }
}
