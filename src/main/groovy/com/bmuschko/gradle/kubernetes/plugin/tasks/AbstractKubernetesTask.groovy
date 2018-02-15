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

import com.bmuschko.gradle.kubernetes.plugin.utils.KubernetesContextLoader
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

abstract class AbstractKubernetesTask extends AbstractReactiveStreamsTask {

    /**
     * Classpath for Kubernetes client libraries.
     */
    @InputFiles
    @Optional
    FileCollection classpath

    /**
     * Kubernetes master URL.
     */
    @Input
    @Optional
    String url

    @Internal
    KubernetesContextLoader contextLoader

    @Override
    void runReactiveStream() {
        runInKubernetesClassPath { dockerClient ->
            runRemoteCommand(dockerClient)
        }
    }

    void runInKubernetesClassPath(Closure closure) {
        contextLoader.withClasspath(getClasspath()?.files, closure)
    }

    abstract void runRemoteCommand(dockerClient)
}
