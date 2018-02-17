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
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

/*
 * Responsible for passing along a fully created and loaded `KubernetesClient` to the
 * downstream task that extends this class.
 */
abstract class AbstractKubernetesTask extends AbstractReactiveStreamsTask {

    @Internal
    KubernetesContextLoader contextLoader

    @Override
    void runReactiveStream() {
        runInKubernetesClassPath { kubernetesClient ->
            runRemoteCommand(kubernetesClient)
        }
    }

    void runInKubernetesClassPath(final Closure closure) {
        contextLoader.withClasspath(closure)
    }

    abstract void runRemoteCommand(kubernetesClient)
}
