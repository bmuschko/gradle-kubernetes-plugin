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

import com.bmuschko.gradle.kubernetes.plugin.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult

/**
 *
 * All functional tests for the `CreateDeployment` task.
 *
 */
class CreateDeploymentFunctionalTest extends AbstractFunctionalTest {

    def defaultNamespace = 'default'

    def "Create deployment from resource"() {

        def instanceName = randomString()
        def tokenMap = ['DEPLOYMENT_NAME' : instanceName,
                        'REPLICA_COUNT' : 3,
                        'PORT' : 80]
        def destinationFile = new File(projectDir, 'temp-nginx-deployment.yaml')
        copyAndReplaceTokensInFile(defaultDeploymentFile, destinationFile, tokenMap)

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.CreateDeployment
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.GetDeployment
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.DeleteDeployment
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.ListDeployments

            import java.util.concurrent.TimeUnit

            task createDeployment(type: CreateDeployment) {
                resource = project.file("${destinationFile.path}")
                namespace = "${defaultNamespace}"

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                    throw exc
                }
                onNext { output ->
                    logger.quiet "$SHOULD_REACH_HERE: next=\${output}"
                }
                onComplete {
                    logger.quiet '$ON_COMPLETE_REACHED'
                }
                doLast {
                    if (response()) {
                        logger.quiet '$RESPONSE_SET_MESSAGE'
                    }
                }
            }

            task getDeployment(type: GetDeployment, dependsOn: createDeployment){
                deployment = "${instanceName}"
                namespace = "${defaultNamespace}"
                retry {
                    withDelay(30, TimeUnit.SECONDS)
                    withMaxRetries(6)
                }
            }

            task listDeployments(type: ListDeployments, dependsOn: getDeployment) {
                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                    throw exc
                }
                onNext { output ->
                    if (output.getMetadata().getName() == "${instanceName}") {
                        logger.quiet 'Found our INSTANCE'
                    }
                }
            }

            task deleteDeployment(type: DeleteDeployment){
                deployment = "${instanceName}"
                namespace = "${defaultNamespace}"
                gracePeriod = 5000
            }

            task workflow(dependsOn: listDeployments) {
                finalizedBy deleteDeployment
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating deployment...')
            result.output.contains('Getting deployment...')
            result.output.contains('Deleting deployment...')
            result.output.contains('Listing deployments...')
            result.output.contains('Found our INSTANCE')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }

    /**
     *
     *  Recreating example from:
     *
     *      https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
     *      
     */
    def "Create deployment"() {

        def instanceName = randomString()

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.CreateDeployment
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.GetDeployment
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.DeleteDeployment
            import java.util.concurrent.TimeUnit

            task createDeployment(type: CreateDeployment) {
                deployment = "${instanceName}"
                namespace = "${defaultNamespace}"
                withSelectorLabels = ['app' : 'nginx']
                replicas = 3
                pod {
                    addContainer('nginx-container', 'nginx', null, null, null).withPorts(80, null)
                }

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                    throw exc
                }
                onNext { output ->
                    logger.quiet "$SHOULD_REACH_HERE: next=\${output}"
                }
                onComplete {
                    logger.quiet '$ON_COMPLETE_REACHED'
                }
                doLast {
                    if (response()) {
                        logger.quiet '$RESPONSE_SET_MESSAGE'
                    }
                }
            }

            task getDeployment(type: GetDeployment, dependsOn: createDeployment){
                deployment = "${instanceName}"
                namespace = "${defaultNamespace}"
                retry {
                    withDelay(30, TimeUnit.SECONDS)
                    withMaxRetries(6)
                }
            }

            task deleteDeployment(type: DeleteDeployment){
                deployment = "${instanceName}"
                namespace = "${defaultNamespace}"
                gracePeriod = 5000
            }

            task workflow(dependsOn: getDeployment) {
                finalizedBy deleteDeployment
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating deployment...')
            result.output.contains('Getting deployment...')
            result.output.contains('Deleting deployment...')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }

    def "Create deployment and execute reactive-streams with config"() {

        def instanceName = randomString()
        def randomPod = randomString()

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.CreateDeployment
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.GetDeployment
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.DeleteDeployment
            import java.util.concurrent.TimeUnit

            task createDeployment(type: CreateDeployment) {
                def labels = ['some-label' : "${randomString()}"]
                def annos = ['some-anno' : "${randomString()}"]
                config {
                    editOrNewMetadata()
                    .withName("${instanceName}")
                    .withNamespace("${defaultNamespace}")
                    .withLabels(labels)
                    .withAnnotations(annos)
                    .endMetadata()
                }
                pod {
                    pod = "${randomPod}"
                    addContainer('nginx-container', 'nginx', null, null, null).withPorts(80, null)
                }

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                    throw exc
                }
                onNext { output ->
                    logger.quiet "$SHOULD_REACH_HERE: next=\${output}"
                }
                onComplete {
                    logger.quiet '$ON_COMPLETE_REACHED'
                }
                doLast {
                    if (response()) {
                        logger.quiet '$RESPONSE_SET_MESSAGE'
                    }
                }
            }

            task getDeployment(type: GetDeployment, dependsOn: createDeployment){
                deployment = "${instanceName}"
                namespace = "${defaultNamespace}"
                retry {
                    withDelay(30, TimeUnit.SECONDS)
                    withMaxRetries(6)
                }
            }

            task deleteDeployment(type: DeleteDeployment){
                deployment = "${instanceName}"
                namespace = "${defaultNamespace}"
                gracePeriod = 5000
            }

            task workflow(dependsOn: getDeployment) {
                finalizedBy deleteDeployment
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating deployment...')
            result.output.contains('Getting deployment...')
            result.output.contains('Deleting deployment...')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }
}
