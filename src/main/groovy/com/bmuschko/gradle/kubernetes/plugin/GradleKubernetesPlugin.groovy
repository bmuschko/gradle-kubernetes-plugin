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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection

/**
 * Gradle plugin that provides custom tasks for interacting with Kubernetes.
 */
class GradleKubernetesPlugin implements Plugin<Project> {

    public static final String KUBERNETES_CLIENT_CONFIGURATION_NAME = 'kubernetesClient'
    public static final String KUBERNETES_CLIENT_DEFAULT_VERSION = '3.1.10'
    public static final String EXTENSION_NAME = 'kubernetes'
    public static final String DEFAULT_TASK_GROUP = 'Kubernetes'

    @Override
    void apply(final Project project) {

        // if no repositories were defined fallback to buildscript
        // repositories to resolve dependencies as a last resort
        project.afterEvaluate {
            if (project.repositories.size() == 0) {
                project.repositories.addAll(project.buildscript.repositories.collect())
            }
        }

        // configure all tasks for execution
        configureAbstractKubernetesTask(project)
    }

    /*
     * Configure all instances of AbsractKubernetesTask for dynamic execution.
     */
    private void configureAbstractKubernetesTask(final Project project) {
        final GradleKubernetesContextLoader kubernetesContextLoader = createGradleKubernetesContextLoader(project)
        project.tasks.withType(AbstractKubernetesTask) {
            group = DEFAULT_TASK_GROUP
            contextLoader = kubernetesContextLoader
        }
    }

    /*
     * Create the GradleKubernetesContextLoader required for execution.
     */
    private GradleKubernetesContextLoader createGradleKubernetesContextLoader(final Project project) {
        final Configuration kubernetesConfiguration = getKubernetesClientConfiguration(project)
        final GradleKubernetesExtension kubernetesExtension = getGradleKubernetesExtension(project)
        new GradleKubernetesContextLoader(kubernetesConfiguration, kubernetesExtension)
    }

    /*
     * Get, and possibly create, the Kubernetes client configuration. We've coded things
     * in such a way that it is possible for the end-user, should they really want to,
     * to define their own kubernetes-client-config to use for execution.
     */
    private Configuration getKubernetesClientConfiguration(final Project project) {
        final Configuration possibleConfig = project.configurations.findByName(KUBERNETES_CLIENT_CONFIGURATION_NAME)
        if (possibleConfig) {
            possibleConfig
        } else {
            final createdConfig = project.configurations.create(KUBERNETES_CLIENT_CONFIGURATION_NAME)
                .setVisible(false)
                .setTransitive(true)
                .setDescription('The Kubernetes java client used by this plugin.')
                .defaultDependencies { dependencies ->
                    final def kubeDep = project.dependencies.create("io.fabric8:kubernetes-openshift-uberjar:$KUBERNETES_CLIENT_DEFAULT_VERSION")
                    dependencies.add(kubeDep)
                    final def slf4jDep = project.dependencies.create('org.slf4j:slf4j-simple:1.7.5')
                    dependencies.add(slf4jDep)
                }
                .exclude(group: 'org.slf4j')
                .exclude(group: 'log4j')

            createdConfig
        }
    }

    /*
     * Get, and possibly create, the GradleKubernetesExtension.
     */
    private GradleKubernetesExtension getGradleKubernetesExtension(final Project project) {
        final def possibleExtension = project.extensions.findByName(EXTENSION_NAME)
        if (possibleExtension) {
            possibleExtension
        } else {
            project.extensions.create(EXTENSION_NAME, GradleKubernetesExtension)
        }
    }
}
