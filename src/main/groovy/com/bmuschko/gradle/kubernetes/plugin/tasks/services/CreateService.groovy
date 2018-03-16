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
import org.gradle.api.Nullable

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

/**
 * Create a service.
 */
class CreateService extends AbstractKubernetesTask {

    @Input
    @Optional
    String namespace

    @Input
    @Optional
    String service

    @Input
    @Optional
    Map<String, String> selector

    @Internal
    private ServiceSpec serviceSpec = new ServiceSpec()

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
        obj = invokeMethod(obj, 'editOrNewMetadata')
        obj = invokeMethod(obj, 'withName', service)
        obj = invokeMethod(obj, 'withNamespace', namespace)
        obj = invokeMethod(obj, 'endMetadata')

        obj = invokeMethod(obj, 'editOrNewSpec')
        obj = invokeMethod(obj, 'withType', this.serviceSpec.type)
        obj = invokeMethod(obj, 'addNewPort')
        obj = invokeMethod(obj, 'withPort', this.serviceSpec.port)
        obj = invokeMethod(obj, 'withNodePort', this.serviceSpec.nodePort)
        obj = invokeMethod(obj, 'withNewTargetPort', this.serviceSpec.targetPort)
        obj = invokeMethod(obj, 'withProtocol', this.serviceSpec.protocol)
        obj = invokeMethod(obj, 'endPort')
        obj = invokeMethod(obj, 'withSelector', this.selector)
        invokeMethod(obj, 'endSpec') 
    }

    public void addSpec(@Nullable String type,
            @Nullable Integer port,
            @Nullable Integer nodePort,
            @Nullable Integer targetPort,
            @Nullable String protocol) {

        final String localType = type?.trim() ?: 'ClusterIp'
        this.serviceSpec = new ServiceSpec(type: localType,
            port: port,
            nodePort: nodePort,
            targetPort: targetPort,
            protocol: protocol)
    }

    static class ServiceSpec {
        public String type // ClusterIp, NodePart, etc
        public Integer port // port exposed to all pods
        public Integer nodePort // port exposed to world
        public Integer targetPort // port exposed from container
        public String protocol // UDP, TCP, etc
    }
}