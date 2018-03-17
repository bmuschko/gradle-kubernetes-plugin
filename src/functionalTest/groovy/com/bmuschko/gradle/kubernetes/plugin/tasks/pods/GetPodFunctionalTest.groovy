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

package com.bmuschko.gradle.kubernetes.plugin.tasks.pods

import com.bmuschko.gradle.kubernetes.plugin.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult

/**
 *
 * All functional tests for the `GetPod` task.
 *
 */
class GetPodFunctionalTest extends AbstractFunctionalTest {

    def "Get non-existent pod"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.GetPod

            task getPod(type: GetPod) {
                pod = "${randomString()}"

                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: getPod)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting pod...')
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains('Pod could not be found.')
    }

    def "Get non-existent pod with config"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.GetPod

            task getPod(type: GetPod) {
                config {
                    withName("${randomString()}")
                }

                onError { exc ->
                    logger.quiet "$SHOULD_REACH_HERE value=\${exc}"
                }
            }

            task workflow(dependsOn: getPod)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting pod...')
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains('Pod could not be found.')
    }
}
