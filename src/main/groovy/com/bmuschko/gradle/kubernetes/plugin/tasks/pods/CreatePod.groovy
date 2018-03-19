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

package com.bmuschko.gradle.kubernetes.plugin.tasks.pods

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import org.gradle.api.GradleException
import org.gradle.api.Nullable

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

/**
 * Create a pod.
 */
class CreatePod extends AbstractKubernetesTask {

    // namespace to create pod in
    @Input
    @Optional
    String namespace

    // name of pod
    @Input
    @Optional
    String pod

    @Input
    @Optional
    Map<String, String> withLabels
    
    @Internal
    private List<Container> containers = []

    // Can be a String path to a File, a File object, a URL, or an InputStream
    @Input
    @Optional
    def resource

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Creating pod...'
        def objToConfigure
        if (resource) {
            def podObj = kubernetesClient.pods().load(resource).get()
            objToConfigure = kubernetesClient.pods().createNew()
                                .withApiVersion(podObj.getApiVersion())
                                .withKind(podObj.getKind())
                                .withMetadata(podObj.getMetadata())
                                .withSpec(podObj.getSpec())
                                .withStatus(podObj.getStatus())
        } else {
            objToConfigure = kubernetesClient.pods().createNew()
        }

        // configure on meta-data
        def objReconfigured = configureOn(objToConfigure)

        // apply user-defined inputs
        def objWithUserInputs = applyInputs(objReconfigured)

        // finalize creation of pod
        def localResponse = objWithUserInputs.done()

        // register response for downstream use which in this case
        // is just a `Pod` instance.
        responseOn(localResponse)
    }

    @Override
    def applyInputs(obj) {
        def objRef = wrapAtomic(obj)
        invokeMethod(objRef, 'editOrNewMetadata')
        if (!objRef.get().getName()) {
            invokeMethod(objRef, 'withName', pod ?: randomString('gkp-pod-'))
        } 
        invokeMethod(objRef, 'withNamespace', namespace)
        invokeMethod(objRef, 'withLabels', withLabels)
        invokeMethod(objRef, 'endMetadata')

        if (this.containers) {
            invokeMethod(objRef, 'editOrNewSpec')
            this.containers.each { cont ->
                invokeMethod(objRef, 'addNewContainer')
                invokeMethod(objRef, 'withCommand', cont.cmd)
                invokeMethod(objRef, 'withArgs', cont.args)
                if (!objRef.get().getName()) {
                    invokeMethod(objRef, 'withName', cont.name ?: randomString('gkp-container-'))
                } 
                invokeMethod(objRef, 'withImage', cont.image)
                invokeMethod(objRef, 'addNewPort')
                invokeMethod(objRef, 'withContainerPort', cont.containerPort)
                invokeMethod(objRef, 'endPort')
                invokeMethod(objRef, 'endContainer')
            }
            invokeMethod(objRef, 'endSpec')
        }
        objRef.get()
    }

    public void addContainer(@Nullable String name, @Nullable String image, @Nullable Integer containerPort) {
        addContainer(name, image, containerPort, null, null)
    }

    public void addContainer(@Nullable String name,
            @Nullable String image,
            @Nullable Integer containerPort,
            @Nullable Integer cmd,
            @Nullable Integer args) {

        final Container cont = new Container(name: name,
            image: image,
            containerPort: containerPort,
            cmd: cmd,
            args: args)

        this.containers.add(cont)
    }

    static class Container {
        public String name // name of container
        public String image // name of image
        public Integer containerPort // port exposed to pod
        public List<String> cmd // command to use when invoking container
        public List<String> args // args to use to pass to command when invoking container
    }
}
