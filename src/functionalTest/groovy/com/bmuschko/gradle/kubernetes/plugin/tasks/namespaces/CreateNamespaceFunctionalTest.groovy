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

package com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces

import com.bmuschko.gradle.kubernetes.plugin.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult

/**
 *
 * All functional tests for the `CreateNamespace` task.
 *
 */
class CreateNamespaceFunctionalTest extends AbstractFunctionalTest {

    def randomNamespace = randomString()

    def "Create namespace with dynamic name, execute reactive-streams"() {
        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.CreateNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.DeleteNamespace

            task createNamespace(type: CreateNamespace) {
                doFirst {
                    namespace = "${randomNamespace}"
                }
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

            task deleteNamespace(type: DeleteNamespace) {
                doFirst {
                    namespace = "${randomNamespace}"
                }
                gracePeriod = 5000

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: \${exc}"
                }
            }

            task workflow(dependsOn: createNamespace) {
                finalizedBy deleteNamespace
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating namespace...')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }

    def "Create namespace with dynamic name, execute reactive-streams, and with no config"() {

        def randomNamespace = randomString()

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.CreateNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.DeleteNamespace

            task createNamespace(type: CreateNamespace) {
                namespace = "${randomNamespace}"
                withLabels = ["${randomString()}" : "${randomString()}"]

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

            task deleteNamespace(type: DeleteNamespace) {
                doFirst {
                    namespace = "${randomNamespace}"
                }
                gracePeriod = 5000

                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: \${exc}"
                }
            }

            task workflow(dependsOn: createNamespace) {
                finalizedBy deleteNamespace
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating namespace...')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }

    def "Create namespace, execute reactive-streams, and with config"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.CreateNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.DeleteNamespace

            task createNamespace(type: CreateNamespace) {
                config {
                    withName("${randomString()}")
                }
                onError { exc ->
                    logger.quiet "$ON_ERROR_NOT_REACHED: exc=\${exc}"
                }
                onNext { output ->
                    if (output) {
                        logger.quiet "$ON_NEXT_REACHED with name \${output.getMetadata().getName()}"
                    }
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

            task deleteNamespace(type: DeleteNamespace) {
                doFirst {
                    namespace = tasks.createNamespace.response().getMetadata().getName()
                }
                gracePeriod = 5000
            }

            task workflow(dependsOn: createNamespace) {
                finalizedBy deleteNamespace
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating namespace...')
            !result.output.contains(ON_ERROR_NOT_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
            result.output.contains(ON_NEXT_REACHED)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }
}
