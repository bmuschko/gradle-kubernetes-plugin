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
 * All functional tests for the `GetDeployment` task.
 *
 */
class GetDeploymentFunctionalTest extends AbstractFunctionalTest {

    def "Get non-existent deployment"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.GetDeployment

            task getDeployment(type: GetDeployment) {
                deployment = "${randomString()}"
                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: getDeployment)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting deployment...')
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains('could not be found.')
    }

    def "Get non-existent deployment with config"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.deployments.GetDeployment

            task getDeployment(type: GetDeployment) {
                config {
                    withName("${randomString()}")
                }

                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: getDeployment)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting deployment...')
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains('could not be found.')
    }
}
