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

import com.bmuschko.gradle.kubernetes.plugin.tasks.AbstractKubernetesTask
import com.bmuschko.gradle.kubernetes.plugin.utils.KubernetesContextLoader

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection

/**
 * Gradle plugin that provides custom tasks for interacting with Kubernetes.
 */
class GradleKubernetesPlugin implements Plugin<Project> {

    public static final String KUBERNETES_CLIENT_CONFIGURATION_NAME = 'kubernetesClient'
    public static final String KUBERNETES_CLIENT_DEFAULT_VERSION = '3.1.8'
    public static final String EXTENSION_NAME = 'kubernetes'
    public static final String DEFAULT_TASK_GROUP = 'Kubernetes'

    @Override
    void apply(Project project) {

        // if no repositories were defined fallback to buildscript
        // repositories to resolve dependencies as a last resort
        project.afterEvaluate {
            if (project.repositories.size() == 0) {
                project.repositories.addAll(project.buildscript.repositories.collect())
            }
        }

        final Configuration config = project.configurations
            .create(KUBERNETES_CLIENT_CONFIGURATION_NAME)
            .setVisible(false)
            .setTransitive(true)
            .setDescription('The Kubernetes java client used by this project.')
            .defaultDependencies { dependencies ->
                def kubeDep = project.dependencies.create("io.fabric8:kubernetes-openshift-uberjar:$KUBERNETES_CLIENT_DEFAULT_VERSION")
                dependencies.add(kubeDep)
            }

        final GradleKubernetesExtension extension = project.extensions.create(EXTENSION_NAME, GradleKubernetesExtension)
        extension.classpath = config

        configureAbstractKubernetesTask(project, extension)
    }

    private void configureAbstractKubernetesTask(final Project project, final GradleKubernetesExtension extension) {
        def kubernetesContextLoader = new KubernetesContextLoader(extension)
        project.tasks.withType(AbstractKubernetesTask) {
            group = DEFAULT_TASK_GROUP
            contextLoader = kubernetesContextLoader
        }
    }
}
