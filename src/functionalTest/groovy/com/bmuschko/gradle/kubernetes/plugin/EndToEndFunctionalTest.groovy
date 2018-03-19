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

package com.bmuschko.gradle.kubernetes.plugin

import com.bmuschko.gradle.kubernetes.plugin.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult

/**
 *
 * End-To-End functional test.
 * 
 * The point of this tester is to use all tasks in and end-to-end
 * workflow to show how a "complete" pod, with all the bells and
 * whistles, can be stood up.
 *
 */
class EndToEndFunctionalTest extends AbstractFunctionalTest {

    def "Create namespace, then create service, then create pod"() {

        def randomNamespace = 'end-to-end-namespace'
        def randomService = 'end-to-end-service'
        def randomPod = 'end-to-end-pod'

        def tokenMap = ['POD_NAME' : randomPod,
                        'CONTAINER_NAME' : "${randomPod}-container",
                        'PORT' : 80]
        def destinationFile = new File(projectDir, 'temp-nginx-pod.yaml')
        copyAndReplaceTokensInFile(defaultPodFile, destinationFile, tokenMap)

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.CreateNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.GetNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.DeleteNamespace

            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.CreateService
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.GetService
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.DeleteService

            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.CreatePod
            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.GetPod
            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.DeletePod

            import java.util.concurrent.TimeUnit

            kubernetes {
                retry {
                    withDelay(5, TimeUnit.SECONDS)
                    withMaxRetries(3)
                }
            }

            task createNamespace(type: CreateNamespace) {
                description = 'Create namespace to work within'
                namespace = "${randomNamespace}"
                withLabels = ['name' : 'end-to-end-namespace-label']

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task getNamespace(type: GetNamespace, dependsOn: createNamespace) {
                description = 'Wait until namespace is available'
                namespace = "${randomNamespace}"

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task createService(type: CreateService, dependsOn: getNamespace) {
                description = 'Create service for our pod to reach outside world'
                service = "${randomService}"
                namespace = "${randomNamespace}"
                withLabels = ['name' : 'end-to-end-service-label']
                selector = ['name' : 'end-to-end-pod-label']
                addSpec('NodePort', 32333, 5432, 5432, 'TCP')

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task getService(type: GetService, dependsOn: createService) {
                description = 'Wait until service is available'
                service = "${randomService}"
                namespace = "${randomNamespace}"

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task createPod(type: CreatePod, dependsOn: getService) {
                description = 'Create end-to-end pod'
                pod = "${randomPod}"
                namespace = "${randomNamespace}"
                withLabels = ['name' : 'end-to-end-pod-label']
                addContainer('end-to-end-container', 'postgres:10.3', 5432)

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task getPod(type: GetPod, dependsOn: createPod) {
                description = 'Wait until pod is available'
                pod = "${randomPod}"
                namespace = "${randomNamespace}"

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task deletePod(type: DeletePod) {
                pod = "${randomPod}"
                namespace = "${randomNamespace}"
            }

            task deleteService(type: DeleteService, dependsOn: deletePod) {
                service = "${randomService}"
                namespace = "${randomNamespace}"
            }

            task deleteNamespace(type: DeleteNamespace, dependsOn: deleteService) {
                namespace = "${randomNamespace}"
            }

            task workflow(dependsOn: getPod) {
                finalizedBy deleteNamespace
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating namespace...')
            result.output.contains('Creating service...')
            result.output.contains('Creating pod...')
            result.output.contains('Deleting namespace...')
            result.output.contains('Deleting service...')
            result.output.contains('Deleting pod...')
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }
}
