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
 * All functional tests for the `ListNamespaces` task.
 *
 */
class ListNamespacesFunctionalTest extends AbstractFunctionalTest {

    def "List namespaces, execute reactive-streams, with no config"() {
        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

            task listNamespaces(type: ListNamespaces) {
                onError { exc ->
                    logger.quiet "$ON_ERROR_NOT_REACHED exception=\${exc}"
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

            task workflow(dependsOn: listNamespaces)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Listing namespaces...')
            !result.output.contains(ON_ERROR_NOT_REACHED)
            result.output.contains(ON_NEXT_REACHED)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }

    def "List namespaces, execute reactive-streams, and with config"() {
        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

            task listNamespaces(type: ListNamespaces) {
                config {
                    withLabel("${randomString()}")
                }
                onError { exc ->
                    logger.quiet "$ON_ERROR_NOT_REACHED: exception=\${exc}"
                }
                onNext { output ->
                    logger.quiet '$SHOULD_NOT_REACH_HERE'
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

            task workflow(dependsOn: listNamespaces)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Listing namespaces...')
            !result.output.contains(ON_ERROR_NOT_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }

    def "List non-existent namespaces"() {
        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

            task listNamespaces(type: ListNamespaces) {
                withLabels = ["${randomString()}" : "${randomString()}"]
                withoutLabels = ["${randomString()}" : "${randomString()}"]

                onError { exc ->
                    logger.quiet "$ON_ERROR_NOT_REACHED: exception=\${exc}"
                }
                onNext { output ->
                    logger.quiet '$SHOULD_NOT_REACH_HERE'
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

            task workflow(dependsOn: listNamespaces)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Listing namespaces...')
            !result.output.contains(ON_ERROR_NOT_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }
}
