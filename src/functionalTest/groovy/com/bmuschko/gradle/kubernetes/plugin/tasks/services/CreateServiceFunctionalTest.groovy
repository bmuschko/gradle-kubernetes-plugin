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

import com.bmuschko.gradle.kubernetes.plugin.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult

/**
 *
 * All functional tests for the `CreateService` task.
 *
 */
class CreateServiceFunctionalTest extends AbstractFunctionalTest {

    def defaultNamespace = 'default'

    def "Create service and execute reactive-streams"() {

        def randomService = randomString()

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.CreateService
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.DeleteService
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.GetService
            import java.util.concurrent.TimeUnit

            task createService(type: CreateService) {
                service = "${randomService}"
                namespace = "${defaultNamespace}"
                type = 'NodePort'
                addPorts(null, 'TCP', 32333, 12345, 8080,).addPorts('special-name', 'TCP', 32334, 12346, 8081)

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
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

            task getService(type: GetService, dependsOn: createService) {
                doFirst {
                    service = "${randomService}"
                    namespace = "${defaultNamespace}"
                }
                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
                retry {
                    withDelay(5, TimeUnit.SECONDS)
                    withMaxRetries(5)
                }
            }

            task deleteService(type: DeleteService) {
                doFirst {
                    service = "${randomService}"
                    namespace = "${defaultNamespace}"
                }
                gracePeriod = 5000

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task workflow(dependsOn: getService) {
                finalizedBy deleteService
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating service...')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }

    def "Create service, using config, and execute reactive-streams"() {

        def randomService = randomString()

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.CreateService
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.DeleteService
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.GetService
            import java.util.concurrent.TimeUnit

            task createService(type: CreateService) {
                config {
                    withNewMetadata()
                    .withName("${randomService}")
                    .withNamespace("${defaultNamespace}")
                    .endMetadata()
                }
                type = 'NodePort'
                addPorts(32333, 12345, 8080)

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
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

            task getService(type: GetService, dependsOn: createService) {
                doFirst {
                    service = "${randomService}"
                    namespace = "${defaultNamespace}"
                }
                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
                retry {
                    withDelay(5, TimeUnit.SECONDS)
                    withMaxRetries(5)
                }
            }

            task deleteService(type: DeleteService) {
                doFirst {
                    service = "${randomService}"
                    namespace = "${defaultNamespace}"
                }
                gracePeriod = 5000

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task workflow(dependsOn: getService) {
                finalizedBy deleteService
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating service...')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }

    def "Create service with illegal Type"() {

        def randomService = randomString()

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.CreateService

            task createService(type: CreateService) {
                config {
                    withNewMetadata()
                    .withName("${randomService}")
                    .withNamespace("${defaultNamespace}")
                    .endMetadata()
                }
                type = 'BlahBlah'
                addPorts(32333, 12345, 8080)

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: exception=\${exc}"
                }
            }

            task workflow(dependsOn: createService)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating service...')
            result.output.contains('No enum constant')
            result.output.contains(SHOULD_NOT_REACH_HERE)
    }
}
