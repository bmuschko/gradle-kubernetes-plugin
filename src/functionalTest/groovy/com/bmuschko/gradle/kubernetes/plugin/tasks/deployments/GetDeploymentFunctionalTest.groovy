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
 * All functional tests for the `GetService` task.
 *
 */
class GetServiceFunctionalTest extends AbstractFunctionalTest {

    def defaultService = 'kubernetes'
    def defaultNamespace = "default"

    def generateName = randomString()
    def "Get default service"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.GetService

            task getService(type: GetService) {
                doFirst {
                    service = "${defaultService}"
                    namespace = "${defaultNamespace}"
                }
                onNext { serv ->
                    logger.quiet "${ON_NEXT_REACHED}: service=\${serv}"
                }
                doLast {
                    if (response().getMetadata().getName() != "${defaultService}") {
                        logger.quiet "$SHOULD_NOT_REACH_HERE: foundName=\${response().getMetadata().getName()}, expected=${defaultService}"
                    } else {
                        logger.quiet '$RESPONSE_SET_MESSAGE'
                    }
                }
                onComplete {
                    logger.quiet "${ON_COMPLETE_REACHED}"
                }
            }

            task workflow(dependsOn: getService)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting service...')
            !result.output.contains(SHOULD_NOT_REACH_HERE)
            result.output.contains(ON_NEXT_REACHED)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }

    def "Get non-existent service"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.GetService

            task getService(type: GetService) {
                service = "${randomString()}"
                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: getService)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting service...')
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains('could not be found.')
    }

    def "Get non-existent service with config"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.GetService

            task getService(type: GetService) {
                config {
                    withName("${randomString()}")
                }

                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: getService)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting service...')
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains('could not be found.')
    }
}
