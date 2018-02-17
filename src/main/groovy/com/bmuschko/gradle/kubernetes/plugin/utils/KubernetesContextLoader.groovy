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

package com.bmuschko.gradle.kubernetes.plugin.utils

import com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesExtension

import org.gradle.util.ConfigureUtil
import org.gradle.api.file.FileCollection

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 *
 *  Responsible for setting up the context to execute various commands with
 *  the `KubernetesClient`. To make things as efficient as possible, and
 *  potentially not very gradle-like, we load the client jars dynamically
 *  at runtime and only when they are first accessed.
 *
 */
class KubernetesContextLoader {

    private final FileCollection kubernetesFileCollection
    private final GradleKubernetesExtension kubernetesExtension
    private def kubernetesClient // lazily created `KubernetesClient`

    public KubernetesContextLoader(final FileCollection kubernetesFileCollection,
                                    final GradleKubernetesExtension kubernetesExtension) {
        this.kubernetesFileCollection = kubernetesFileCollection
        this.kubernetesExtension = kubernetesExtension
    }

    void withClasspath(final Closure closure) {
        final ClassLoader originalClassLoader = getClass().classLoader

        try {
            Thread.currentThread().contextClassLoader = createClassLoader(kubernetesFileCollection.files)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.delegate = this
            closure(getKubernetesClient())
        } finally {
            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }

    /**
     * Creates the classloader with the given classpath files.
     *
     * @param classpathFiles Classpath files
     * @return URL classloader
     */
    private URLClassLoader createClassLoader(final Set<File> classpathFiles) {
        new URLClassLoader(toURLArray(classpathFiles), ClassLoader.systemClassLoader.parent)
    }

    /**
     * Creates URL array from a set of files.
     *
     * @param files Files
     * @return URL array
     */
    private URL[] toURLArray(final Set<File> files) {
        files.collect { file -> file.toURI().toURL() } as URL[]
    }

    /**
     * Get, and possibly create, the `KubernetesClient` instance.
     */
    synchronized def getKubernetesClient() {
        if (!kubernetesClient) {
            final Class configClass = loadClass('io.fabric8.kubernetes.client.Config')
            final Class configBuilderClass = loadClass('io.fabric8.kubernetes.client.ConfigBuilder')
            def configBuilder = configBuilderClass.getConstructor().newInstance();
            if (kubernetesExtension.config()) {
                configBuilder = ConfigureUtil.configure(kubernetesExtension.config(), configBuilder)
            }

            final Class clientClass = loadClass('io.fabric8.kubernetes.client.DefaultKubernetesClient')
            def clientConstructor = clientClass.getConstructor(configClass)
            kubernetesClient = clientConstructor.newInstance(configBuilder.build());
        }

        kubernetesClient
    }

    /**
     * {@inheritDoc}
     */
    Class loadClass(final String className) {
        Thread.currentThread().contextClassLoader.loadClass(className)
    }
}

