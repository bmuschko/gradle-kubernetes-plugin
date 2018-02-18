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

import org.gradle.util.ConfigureUtil

/**
 *  Trait that provides a generic `config` API.
 *
 *  The idea here is that the implementing class can have potentially
 *  X number of `config` closures set which, and when calling `configureOn(delegate)`,
 *  we will apply each of those to an arbitrary Object (assuming it can done).
 *  
 *  A typical example would look like:
 *
 *      kubernetes {
 *          config {
 *              withMasterUrl("https://google.com")
 *              withUsername("hello")
 *              withPassword("world")
 *          }
 *      }
 *  
 *  Or when applying to a task:
 *  
 *      task listAllNamespaces(type: ListNamespaces) {
 *          config {
 *              setSomeProperty("hello")
 *              setAnotherProperty("world")
 *          }
 *          onNext { ns ->
 *              logger.quiet "Found namespace: ${ns}"
 *          }
 *      }
 *
 *  Multiple `config` closures are allowed to be set thus allowing the
 *  calling code to potentially share `config` objects between tasks/etc.
 *  You might that you need to do something like:
 *
 *      def sharedConfig = {
 *          setSomeSharedProperty("HelloWorld")
 *      }
 *
 *      task HelloWorldOne(type: ListNamespaces) {
 *          config {
 *              specificPropertyToSet("hello")
 *          }
 *          config sharedConfig
 *      }
 *      
 *      task HelloWorldTwo(type: ListNamespaces) {
 *          config {
 *              mySpecificPropertyToSet("world")
 *          }
 *          config sharedConfig
 *      }
 */
trait ConfigAware {

    // list of configs to apply against an arbitrary delegate.
    private final List<Closure> config = []

    /**
     *  Add a Closure to configure against an arbitrary object.
     */
    void config(final Closure closure) {
        if (closure) {
            config.add(closure)
        }
    }

    /**
     *  Configure all closures on passed delegate.
     */
    def configureOn(final def delegate) {
        if (config && delegate) {
            config.each { passedConfig ->
                ConfigureUtil.configure(passedConfig, delegate)
            }
        }
        delegate
    }
}

