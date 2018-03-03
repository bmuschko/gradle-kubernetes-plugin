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
 * All functional tests for the `GetNamespace` task.
 *
 */
class GetNamespaceFunctionalTest extends AbstractFunctionalTest {

    def generateName = randomString()
    def "Create, Get, Delete and then List namespaces"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.CreateNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.DeleteNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.GetNamespace
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.ListNamespaces

            task createNamespace(type: CreateNamespace) {
                doFirst {
                    logger.quiet "Creating namespace ${generateName}"
                }
                config {
                    withName("${generateName}")
                }
            }

            task getNamespace(type: GetNamespace, dependsOn: createNamespace) {
                namespace { tasks.createNamespace.response().getMetadata().getName() }
                doLast {
                    if (response().getMetadata().getName() != "${generateName}") {
                        logger.quiet "$SHOULD_NOT_REACH_HERE: foundName=\${response().getMetadata().getName()}, expected=${generateName}"
                    }
                }
            }

            task deleteNamespace(type: DeleteNamespace, dependsOn: getNamespace) {
                config {
                    withName(tasks.getNamespace.response().getMetadata().getName())
                }
                onError { exc ->
                    logger.quiet "$SHOULD_NOT_REACH_HERE: \${exc}"
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

            task listNamespaces(type: ListNamespaces, dependsOn: deleteNamespace) {
                doFirst {
                  // sleep to give kubernetes time to delete the resource
                  sleep 10000
                }
                onNext { output ->
                    if (output.getMetadata().getName() == "${generateName}") {
                        logger.quiet "$SHOULD_NOT_REACH_HERE with name ${generateName}"
                    }
                }
            }

            task workflow(dependsOn: listNamespaces)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting namespace...')
            result.output.contains('Deleting namespace...')
            !result.output.contains(SHOULD_NOT_REACH_HERE)
            result.output.contains(ON_NEXT_REACHED)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }

    def "Get non-existent namespace"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.GetNamespace

            task getNamespace(type: GetNamespace) {
                namespace { "${randomString()}" }
                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: getNamespace)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting namespace...')
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains('could not be found.')
    }
}
