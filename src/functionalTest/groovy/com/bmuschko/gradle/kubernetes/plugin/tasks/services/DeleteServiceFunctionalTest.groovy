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
 * All functional tests for the `DeleteService` task.
 *
 */
class DeleteServiceFunctionalTest extends AbstractFunctionalTest {

    def "Delete non-existent service"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.DeleteService

            task deleteService(type: DeleteService) {
                service = "${randomString()}"
                gracePeriod = 5000

                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: deleteService)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Deleting service...')
            result.output.contains(SHOULD_REACH_HERE)
    }

    def "Delete non-existent service with config"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.services.DeleteService

            task deleteService(type: DeleteService) {
                config {
                    withName("${randomString()}")
                    .withGracePeriod(5000)
                }

                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: deleteService)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Deleting service...')
            result.output.contains(SHOULD_REACH_HERE)
    }
}
