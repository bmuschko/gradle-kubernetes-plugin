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

package com.bmuschko.gradle.kubernetes.plugin

import org.gradle.testkit.runner.BuildResult
import spock.lang.Requires

/**
 *
 * All functional tests for the `system` package.
 *
 */
class KubernetesSystemFunctionalTest extends AbstractFunctionalTest {

    def "Can get Kubernetes Configuration and execute reactive-streams"() {
        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.system.Configuration

            task kubeConfig(type: Configuration) {
                onError {
                    logger.quiet '$ON_ERROR_NOT_REACHED'
                }
                onNext { output ->
                    if (output) {
                        logger.quiet '$ON_NEXT_REACHED'
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

            task workflow(dependsOn: kubeConfig)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Api-Version: ')
            result.output.contains('Master-URL: ')
            !result.output.contains(ON_ERROR_NOT_REACHED)
            result.output.contains(ON_NEXT_REACHED)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }
}
