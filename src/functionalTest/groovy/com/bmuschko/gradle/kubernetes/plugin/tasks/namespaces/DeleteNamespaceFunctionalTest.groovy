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
import spock.lang.Requires

/**
 *
 * All functional tests for the `DeleteNamespace` task.
 *
 */
class DeleteNamespaceFunctionalTest extends AbstractFunctionalTest {

    def "Create namespace, execute reactive-streams, and delete namespace with config"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.CreateNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.DeleteNamespace

            task createNamespace(type: CreateNamespace) {
                config {
                    withName("${randomString()}")
                }
            }

            task deleteNamespace(type: DeleteNamespace, dependsOn: createNamespace) {
                config {
                    withName(tasks.createNamespace.response().getMetadata().getName())
                }
                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: \${exc.message}"
                }
                onNext { output ->
                    if (output) {
                        logger.quiet "$ON_NEXT_REACHED"
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

            task workflow(dependsOn: deleteNamespace)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Deleting namespace...')
            !result.output.contains(SHOULD_NOT_REACH_HERE)
            result.output.contains(ON_NEXT_REACHED)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }

    def "Delete non-existent namespace"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.DeleteNamespace

            task deleteNamespace(type: DeleteNamespace) {
                config {
                    withName("${randomString()}")
                }
                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: deleteNamespace)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Deleting namespace...')
            result.output.contains(SHOULD_REACH_HERE)
    }
}
