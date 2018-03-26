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

import static com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesUtils.randomString

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import com.bmuschko.gradle.kubernetes.plugin.domain.ContainerSpec
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

    @Input
    @Optional
    Map<String, String> volumes = [:] // key=name,value=size-limit (can be null)

    @Internal
    private Map<String, ContainerSpec> containers = [:]

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

            // add requested volumes
            this.volumes.each { volName, sizeLimit ->
                logger.info "Adding volume: ${volName}"
                invokeMethod(objRef, 'addNewVolume')
                invokeMethod(objRef, 'withName', volName)
                invokeMethod(objRef, 'editOrNewEmptyDir')
                invokeMethod(objRef, 'withNewSizeLimit', sizeLimit)
                invokeMethod(objRef, 'endEmptyDir')
                invokeMethod(objRef, 'endVolume')
            }

            // add requested containers
            this.containers.each { cName, cont ->
                logger.info "Adding container: ${cName}"
                invokeMethod(objRef, 'addNewContainer')
                invokeMethod(objRef, 'withName', cName)
                invokeMethod(objRef, 'withImage', cont.image)
                invokeMethod(objRef, 'withCommand', cont.cmd)
                invokeMethod(objRef, 'withArgs', cont.args)

                // add requested container ports
                cont.ports.each { port ->
                    invokeMethod(objRef, 'addNewPort')
                    invokeMethod(objRef, 'withContainerPort', port.containerPort)
                    invokeMethod(objRef, 'withHostPort', port.hostPort)
                    invokeMethod(objRef, 'endPort')
                }

                // add requested environment variables
                cont.envs.each { key, value ->
                    invokeMethod(objRef, 'addNewEnv')
                    invokeMethod(objRef, 'withName', key)
                    invokeMethod(objRef, 'withValue', value)
                    invokeMethod(objRef, 'endEnv')
                }

                // add requested container volume mounts
                cont.volumeMounts.each { volMount ->
                    invokeMethod(objRef, 'addNewVolumeMount')
                    invokeMethod(objRef, 'withName', volMount.name)
                    invokeMethod(objRef, 'withMountPath', volMount.mountPath)
                    invokeMethod(objRef, 'endVolumeMount')
                }

                // add requested liveness probe
                if (cont.livenessProbe) {
                    invokeMethod(objRef, 'editOrNewLivenessProbe')
                    invokeMethod(objRef, 'withPeriodSeconds', cont.livenessProbe.periodSeconds)
                    invokeMethod(objRef, 'withInitialDelaySeconds', cont.livenessProbe.initialDelaySeconds)
                    invokeMethod(objRef, 'withTimeoutSeconds', cont.livenessProbe.timeoutSeconds)
                    invokeMethod(objRef, 'endLivenessProbe')         
                }

                invokeMethod(objRef, 'endContainer')
            }
            invokeMethod(objRef, 'endSpec')
        }
        objRef.get()
    }

    /**
     *  Add a named volume for Pod use.
     *
     *  @volumeName name of volume.
     *  @sizeLimit size of volume or unbounded if null.
     */    
    public void volume(String volumeName, @Nullable String sizeLimit) {
        this.volumes.put(Objects.requireNonNull(volumeName), sizeLimit)
    }

    /**
     *  Add a named container to this pod.
     *
     *  @containerName name of container.
     *  @image the image to use to start container.
     *  @envs environment variables to pass to container (optional).
     *  @cmd the cmd to pass into container (optional).
     *  @args the args to expose to container (optional).
     *  @return the ContainerSpec instance
     */ 
    ContainerSpec addContainer(String containerName,
        String image,
        @Nullable Map<String, String> envs,
        @Nullable List<String> cmd,
        @Nullable List<String> args) {

        final ContainerSpec cont = new ContainerSpec(image: Objects.requireNonNull(image),
            envs: envs,
            cmd: cmd,
            args: args)

        this.containers.put(Objects.requireNonNull(containerName), cont)
        cont
    }
}
