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

package com.bmuschko.gradle.kubernetes.plugin.domain

import com.bmuschko.gradle.kubernetes.plugin.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult

/**
 *
 * Functional tests to exercise the `RetryAware` trait.
 *
 */
class RetryAwareFunctionalTest extends AbstractFunctionalTest {

    def "Retry defined on extension point"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.GetNamespace
            import java.util.concurrent.TimeUnit

            kubernetes {
                retry {
                    withDelay(2, TimeUnit.SECONDS)
                    withMaxRetries(1)
                }
            }

            task getNamespace(type: GetNamespace) {
                namespace = "${randomString()}"
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
            count(result.output, 'Getting namespace...') == 2
    }

    def "Retry defined on task"() {

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.namespaces.GetNamespace
            import java.util.concurrent.TimeUnit

            kubernetes {
                retry {
                    withDelay(2, TimeUnit.SECONDS)
                    withMaxRetries(1)
                }
            }

            // retry defined on task takes precedence
            task getNamespace(type: GetNamespace) {
                namespace = "${randomString()}"
                retry {
                    withDelay(2, TimeUnit.SECONDS)
                    withMaxRetries(2)
                }
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
            count(result.output, 'Getting namespace...') == 3
    }
}
