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

package com.bmuschko.gradle.kubernetes.plugin.tasks.client

import com.bmuschko.gradle.kubernetes.plugin.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult
import spock.lang.Requires

/**
 *
 *  Functional tests for the `KubernetesClient` task.
 *
 */
class KubernetesClientFunctionalTest extends AbstractFunctionalTest {

    def "Can get KubernetesClient instance"() {
        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.client.KubernetesClient

            task kubernetesClient(type: KubernetesClient) {
                onError {
                    logger.quiet '$ON_ERROR_NOT_REACHED'
                }
                onNext { client ->
                    if (client) {
                        logger.quiet "Master URL: \${client.getConfiguration().getMasterUrl()}"
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

            task workflow(dependsOn: kubernetesClient)
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Getting kubernetes-client...')
            result.output.contains('Master URL:')
            !result.output.contains(ON_ERROR_NOT_REACHED)
            result.output.contains(ON_COMPLETE_REACHED)
            result.output.contains(RESPONSE_SET_MESSAGE)
    }
}
