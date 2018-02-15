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
import java.lang.reflect.Method

/**
 *
 */
class KubernetesContextLoader {
    public static final String MODEL_PACKAGE = 'com.github.dockerjava.api.model'
    public static final String COMMAND_PACKAGE = 'com.github.dockerjava.core.command'
    private static final TRAILING_WHIESPACE = /\s+$/

    private final GradleKubernetesExtension kubernetesExtension

    public KubernetesContextLoader(final GradleKubernetesExtension kubernetesExtension) {
        this.kubernetesExtension = kubernetesExtension
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void withClasspath(final Set<File> classpath, final Closure closure) {
        final ClassLoader originalClassLoader = getClass().classLoader

        try {
            Thread.currentThread().contextClassLoader = createClassLoader(classpath ?: kubernetesExtension.classpath?.files)
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
     * Creates Kubernetes client from ClassLoader.
     */
    private getKubernetesClient() {

        // Create configuration
        Class dockerClientConfigClass = loadClass('com.github.dockerjava.core.DockerClientConfig')
        Class dockerClientConfigClassImpl = loadClass('com.github.dockerjava.core.DefaultDockerClientConfig')
        Method dockerClientConfigMethod = dockerClientConfigClassImpl.getMethod('createDefaultConfigBuilder')
        def dockerClientConfigBuilder = dockerClientConfigMethod.invoke(null)
        dockerClientConfigBuilder.withDockerHost(dockerUrl)

        if (dockerCertPath) {
            dockerClientConfigBuilder.withDockerTlsVerify(true)
            dockerClientConfigBuilder.withDockerCertPath(dockerCertPath.canonicalPath)
        } else {
            dockerClientConfigBuilder.withDockerTlsVerify(false)
        }

        if (apiVersion) {
            dockerClientConfigBuilder.withApiVersion(apiVersion)
        }

        def dockerClientConfig = dockerClientConfigBuilder.build()

        // Create client
        Class dockerClientBuilderClass = loadClass('com.github.dockerjava.core.DockerClientBuilder')
        Method method = dockerClientBuilderClass.getMethod('getInstance', dockerClientConfigClass)
        def dockerClientBuilder = method.invoke(null, dockerClientConfig)
        dockerClientBuilder.build()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class loadClass(final String className) {
        Thread.currentThread().contextClassLoader.loadClass(className)
    }
}

