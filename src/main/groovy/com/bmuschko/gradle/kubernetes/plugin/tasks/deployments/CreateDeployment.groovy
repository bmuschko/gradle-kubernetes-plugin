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

import static com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesUtils.randomString

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.CreatePod
import com.bmuschko.gradle.kubernetes.plugin.domain.container.ContainerSpec
import com.bmuschko.gradle.kubernetes.plugin.domain.container.ExecProbe
import com.bmuschko.gradle.kubernetes.plugin.domain.container.HttpProbe

import org.gradle.api.GradleException

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

/**
 * Create a deployment.
 */
class CreateDeployment extends AbstractKubernetesTask implements ContainerSpec {

    // namespace to create deployment in
    @Input
    @Optional
    String namespace

    // name of deployment
    @Input
    @Optional
    String deployment

    @Input
    @Optional
    Map<String, String> withLabels

    @Input
    @Optional
    Map<String, String> withAnnotations

    @Input
    @Optional
    Integer replicas

    @Input
    @Optional
    Map<String, String> withSelectorLabels

    // Can be a String path to a File, a File object, a URL, or an InputStream
    @Input
    @Optional
    def resource

    @Internal
    private Closure<CreatePod> pod

    @Override
    def handleClient(kubernetesClient) {

        logger.quiet 'Creating deployment...'
        def objToConfigure
        if (resource) {
            def loadedObj = kubernetesClient.extensions().deployments().load(resource).get()
            objToConfigure = kubernetesClient.extensions().deployments().createNew()
                                .withApiVersion(loadedObj.getApiVersion())
                                .withKind(loadedObj.getKind())
                                .withMetadata(loadedObj.getMetadata())
                                .withSpec(loadedObj.getSpec())
                                .withStatus(loadedObj.getStatus())
        } else {
            objToConfigure = kubernetesClient.extensions().deployments().createNew()
        }

        // configure on meta-data
        def objReconfigured = configureOn(objToConfigure)

        // apply user-defined inputs
        def objWithUserInputs = applyInputs(objReconfigured)

        // finalize creation of deployment
        def localResponse = objWithUserInputs.done()

        // register response for downstream use which in this case
        // is just a `Deployment` instance.
        responseOn(localResponse)
    }

    @Override
    def applyInputs(obj) {

        def objRef = wrapAtomic(obj)
        invokeMethod(objRef, 'editOrNewMetadata')
        if (!objRef.get().getName()) {
            invokeMethod(objRef, 'withName', deployment ?: randomString('gkp-deployment-'))
        } 
        invokeMethod(objRef, 'withNamespace', namespace)
        invokeMethod(objRef, 'withLabels', withLabels)
        invokeMethod(objRef, 'withAnnotations', withAnnotations)
        invokeMethod(objRef, 'endMetadata')

        invokeMethod(objRef, 'editOrNewSpec')
        invokeMethod(objRef, 'withReplicas', replicas)
        invokeMethod(objRef, 'editOrNewSelector')
        def localSelectorLabels = withSelectorLabels ?: ['gkp-app' : randomString().toString()]
        invokeMethod(objRef, 'withMatchLabels', localSelectorLabels)
        invokeMethod(objRef, 'endSelector')
        
        invokeMethod(objRef, 'editOrNewTemplate')

        // if user did not define a pod closure than we will create one to ensure
        // that the selector-lables are properly applied as they are required.
        Closure<CreatePod> localPod = pod ?: {}
        final CreatePod createPod = project.tasks.create(randomString(), CreatePod)
        localPod.resolveStrategy = Closure.DELEGATE_FIRST
        localPod.delegate = createPod
        localPod.call(createPod)

        // The pod itself needs to have the selector-labels as actual labels
        // so that it can be found and referenced. Here we are simply forcing
        // them to be present and if the user has already defined them then
        // things amount to a no-op.
        def foundLabels = createPod.withLabels != null ? createPod.withLabels : [:]
        foundLabels << localSelectorLabels
        createPod.withLabels = foundLabels
        def podWithInputs = createPod.applyInputs(objRef.get())
        objRef = wrapAtomic(podWithInputs)
        
        invokeMethod(objRef, 'endTemplate')
        invokeMethod(objRef, 'endSpec')
        
        objRef.get()
    }

    /**
     *  Configure an instance of `CreatePod` for this deployment.
     */
    public void pod(Closure<CreatePod> pod) {
        this.pod = pod
    }
}
