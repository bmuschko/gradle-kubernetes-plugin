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
import com.bmuschko.gradle.kubernetes.plugin.domain.container.ContainerSpec
import com.bmuschko.gradle.kubernetes.plugin.domain.container.ExecProbe
import com.bmuschko.gradle.kubernetes.plugin.domain.container.HttpProbe

import org.gradle.api.GradleException

import org.gradle.api.tasks.Input
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

    // Can be a String path to a File, a File object, a URL, or an InputStream
    @Input
    @Optional
    def resource

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
        
        invokeMethod(objRef, 'editOrNewTemplate')
        invokeMethod(objRef, 'editOrNewSpec')

        // add requested containers
        containerSpecs().each { cName, cont ->

            invokeMethod(objRef, 'addNewContainer')
            invokeMethod(objRef, 'withName', cName)
            invokeMethod(objRef, 'withImage', cont.image)
            invokeMethod(objRef, 'withCommand', cont.cmd)
            invokeMethod(objRef, 'withArgs', cont.args)
            
            // add requested termination policy
            if (cont.terminationMessage) {
                invokeMethod(objRef, 'withTerminationMessagePolicy', cont.terminationMessage.policy)
                invokeMethod(objRef, 'withTerminationMessagePath', cont.terminationMessage.path) 
            }

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

                def foundProbe = cont.livenessProbe.probeInstance
                if (foundProbe instanceof ExecProbe) {
                    invokeMethod(objRef, 'editOrNewExec')
                    invokeMethod(objRef, 'withCommand', foundProbe.command)
                    invokeMethod(objRef, 'endExec')
                } else if (foundProbe instanceof HttpProbe) {
                    invokeMethod(objRef, 'editOrNewHttpGet')
                    invokeMethod(objRef, 'withPath', foundProbe.path)
                    invokeMethod(objRef, 'editOrNewPort')
                    invokeMethod(objRef, 'withIntVal', foundProbe.port)
                    invokeMethod(objRef, 'endPort')
                    foundProbe.headers?.each { kHeader, vHeader ->
                        invokeMethod(objRef, 'addNewHttpHeader', kHeader, vHeader)
                    }
                    invokeMethod(objRef, 'endHttpGet')
                } else {
                    throw new GradleException("Must specify a valid probeType: ${foundProbe}")
                }
                invokeMethod(objRef, 'endLivenessProbe')
            }

            // add requested readiness probe
            if (cont.readinessProbe) {
                invokeMethod(objRef, 'withNewReadinessProbe')
                invokeMethod(objRef, 'withPeriodSeconds', cont.readinessProbe.periodSeconds)
                invokeMethod(objRef, 'withInitialDelaySeconds', cont.readinessProbe.initialDelaySeconds)
                invokeMethod(objRef, 'withTimeoutSeconds', cont.readinessProbe.timeoutSeconds)

                def foundProbe = cont.readinessProbe.probeInstance
                if (foundProbe instanceof ExecProbe) {
                    invokeMethod(objRef, 'editOrNewExec')
                    invokeMethod(objRef, 'withCommand', foundProbe.command)
                    invokeMethod(objRef, 'endExec')
                } else if (foundProbe instanceof HttpProbe) {
                    invokeMethod(objRef, 'editOrNewHttpGet')
                    invokeMethod(objRef, 'withPath', foundProbe.path)
                    invokeMethod(objRef, 'editOrNewPort')
                    invokeMethod(objRef, 'withIntVal', foundProbe.port)
                    invokeMethod(objRef, 'endPort')
                    foundProbe.headers?.each { kHeader, vHeader ->
                        invokeMethod(objRef, 'addNewHttpHeader', kHeader, vHeader)
                    }
                    invokeMethod(objRef, 'endHttpGet')
                } else {
                    throw new GradleException("Must specify a valid probeType: ${foundProbe}")
                }
                invokeMethod(objRef, 'endReadinessProbe')
            }

            invokeMethod(objRef, 'endContainer')
        }
        invokeMethod(objRef, 'editOrNewSpec')
        invokeMethod(objRef, 'endTemplate')
        invokeMethod(objRef, 'endSpec')
        
        objRef.get()
    }
}
