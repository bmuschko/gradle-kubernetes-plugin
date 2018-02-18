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

import com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesExtension
import groovy.transform.Synchronized

import org.xeustechnologies.jcl.JarClassLoader
import org.xeustechnologies.jcl.JclObjectFactory

import org.gradle.api.file.FileCollection

/**
 *  Responsible for setting up the context to execute commands with the
 *  `KubernetesClient`. To make things as efficient as possible, and
 *  potentially not very gradle-like, we load the client jars dynamically
 *  at runtime and only when they are first accessed. While this removes
 *  defining the dependencies up front, thus making it potentially un-clear
 *  what this plugin is loading, it gains in the fact that we don't have to
 *  load N number of dependencies up front within the config phase which
 *  can slow down builds trying to get off the ground and doing any actual work.
 *
 *  To isolate execution, and not pollute the broader buildscript classpath, all
 *  required libraries of this plugin are loaded into their own custom class-loader.
 *  As such any classes which need to create objects dynamically and at runtime need
 *  to ensure they load classes from the custom class-loader we create below and not
 *  from some arbitrary source.
 *  
 *  We also create, again lazily, an object factory from the supplied custom
 *  class-loader. Working with the JclObjectFactory can be a bit finicky to use
 *  so we're not requiring it but leaving it open as an option. What we ARE
 *  requiring is for tasks/code, if they need to create/load a class from the
 *  `KubernetesClient` classpath, to ensure they load from the custom class-loader
 *  we initialize below. Failure to do so will cause all kinds of headaches you
 *  probably want to avoid.
 */
class GradleKubernetesContextLoader {

    private final FileCollection kubernetesFileCollection
    private final GradleKubernetesExtension kubernetesExtension
    private def kubernetesClient // lazily created `KubernetesClient`
    private JarClassLoader kubernetesClientClassLoader // lazily created ClassLoader
    private JclObjectFactory kubernetesClientObjectFactory // lazily created object factory from ClassLoader

    public GradleKubernetesContextLoader(final FileCollection kubernetesFileCollection,
                                    final GradleKubernetesExtension kubernetesExtension) {
        this.kubernetesFileCollection = kubernetesFileCollection
        this.kubernetesExtension = kubernetesExtension
    }

    void withClasspath(final Closure closure) {
        initializeKubernetesClassLoader()

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure(getKubernetesClient())
    }

    @Synchronized
    private initializeKubernetesClassLoader() {
        if (!kubernetesClientClassLoader) {

            // 1.) create custom JCL class-loader to store our
            // `KubernetesClient` classpath.
            kubernetesClientClassLoader = new JarClassLoader()

            // 2.) load all requires libraries for `KubernetesClient` into
            // our custom class-loader for isolated execution.
            kubernetesClientClassLoader.addAll(kubernetesFileCollection.files.
                collect { it.toURI().toURL() } as URL[])

            // 3.) OPTIONAL object factory to use for creating objects from
            // our custom class-loader. As it can be a bit finicky to use
            // it's not required to long as calling/creating code loads
            // classes from our custom class-loader and not some other source.
            kubernetesClientObjectFactory = JclObjectFactory.getInstance()
        }
    }

    /**
     * Get, and possibly create, the `KubernetesClient` instance.
     */
    private def getKubernetesClient() {
        if (!kubernetesClient) {

            // 1.) create ConfigBuilder
            final String configBuilderClassName = 'io.fabric8.kubernetes.client.ConfigBuilder'
            def configBuilder = kubernetesClientObjectFactory.create(kubernetesClientClassLoader, configBuilderClassName);

            // 2.) map any configs passed in through extension to the configBuilder instance.
            configBuilder = kubernetesExtension.configureOn(configBuilder)

            // 3.) load `KubernetesClient` from our custom class-loader.
            final String clientClassName = 'io.fabric8.kubernetes.client.DefaultKubernetesClient'
            final Class clientClass = kubernetesClientClassLoader.loadClass(clientClassName)

            // 4.) create `KubernetesClient` instance from our custom class-loader.
            final String configClassName = 'io.fabric8.kubernetes.client.Config'
            final Class configClass = kubernetesClientClassLoader.loadClass(configClassName)
            def clientConstructor = clientClass.getConstructor(configClass)
            kubernetesClient = clientConstructor.newInstance(configBuilder.build());

            // 5.) register shutdown-hook to close kubernetes client.
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    kubernetesClient.close();
                }
            });
        }

        kubernetesClient
    }
}

