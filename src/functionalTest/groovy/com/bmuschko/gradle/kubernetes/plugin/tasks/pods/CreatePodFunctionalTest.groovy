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
 * All functional tests for the `CreatePod` task.
 *
 */
class CreatePodFunctionalTest extends AbstractFunctionalTest {

    def defaultNamespace = 'default'

    def "Create pod and execute reactive-streams"() {

        def randomPod = randomString()

        buildFile << """
            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.CreatePod
            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.GetPod
            import com.bmuschko.gradle.kubernetes.plugin.tasks.pods.DeletePod
            import java.util.concurrent.TimeUnit

            task createPod(type: CreatePod) {
                pod = "${randomPod()}"
                namespace = "${defaultNamespace}"
                labels = ['name' : "${randomPod}-label"]
                addContainer('nginx-container', 'nginx', 'IfNotPresent', 80, null)

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

            task getPod(type: GetPod, dependsOn: createPod){
                pod = "${randomPod()}"
                namespace = "${defaultNamespace}"
                retry {
                    withDelay(10, TimeUnit.SECONDS)
                    withMaxRetries(3)
                }
            }

            task deletePod(type: DeletePod){
                pod = "${randomPod()}"
                namespace = "${defaultNamespace}"
                gracePeriod = 5000
            }

            task workflow(dependsOn: getPod) {
                finalizedBy deletePod
            }
        """

        when:
            BuildResult result = build('workflow')

        then:
            result.output.contains('Creating pod...')
            result.output.contains('Getting pod...')
            result.output.contains('Deleting pod...')
            result.output.contains(RESPONSE_SET_MESSAGE)
            result.output.contains(SHOULD_REACH_HERE)
            result.output.contains(ON_COMPLETE_REACHED)
            !result.output.contains(SHOULD_NOT_REACH_HERE)
    }
}
