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

package com.bmuschko.gradle.kubernetes.plugin.tasks

import com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesContextLoader
import com.bmuschko.gradle.kubernetes.plugin.utils.ConfigAware
import com.bmuschko.gradle.kubernetes.plugin.utils.ResponseAware
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

/*
 * Responsible for passing along a fully created and loaded `KubernetesClient` to the
 * downstream implementing task.
 */
abstract class AbstractKubernetesTask extends AbstractReactiveStreamsTask implements ConfigAware, ResponseAware {

    @Internal
    GradleKubernetesContextLoader contextLoader

    @Override
    def runReactiveStream() {
        def executionResponse
        runInKubernetesClassPath { kubernetesClient ->
            executionResponse = handleClient(kubernetesClient)
        }
        executionResponse
    }

    void runInKubernetesClassPath(final Closure closure) {
        contextLoader.withClasspath(closure)
    }

    /**
     *  Pass the fully created `KubernetesClient` to the implementing
     *  class to do work.
     *
     *  Optionally return a valid Object to pass to super-class
     *  invocation of `onNext` reactive-stream. If it doesn't
     *  make sense to return something than returning a `null`
     *  will suffice.
     */
    abstract def handleClient(kubernetesClient)
}
