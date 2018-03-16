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

package com.bmuschko.gradle.kubernetes.plugin.domain

import org.gradle.api.GradleException

/**
 *  Trait that provides common functions/methods for all Tasks to use.
 *
 *  We decided to create a trait, in favor of say adding methods to AbstractKubernetesTask class,
 *  so as to keep that class as clean as possible and to contain only those methods/functions
 *  that are technically required by downstream tasks to implement or know how to use. This is
 *  to make it stupid-simple for folks contributing for the first time to make it absolutely
 *  clear what a new Task should look like and how to implement it.
 */
trait CommonFunctions {

    private static final def EMPTY_OBJECT_ARRAY = new Object[0]

    /**
     * Check if Object can invoke method, and then do so, otherwise throw Exception. If
     * args is null then `objectToInvoke` is simply returned.
     * 
     * @param objectToInvoke object to invoke method on
     * @param methodName name of method to find and invoke
     * @param args arguments to pass to potential method
     * @return the output, or potentially new Object, created from calling the method
     */
    def invokeMethod(def objectToInvoke, final String methodName, final Object... args) {
        if (args != null) {
            def metaMethod = objectToInvoke.metaClass.getMetaMethod(methodName, args)
            if (metaMethod) {
                metaMethod.invoke(objectToInvoke, args)
            } else {
                throw new GradleException("Cannot invoke method '${methodName}' on class '${objectToInvoke.class}'. Was it previously set?")
            }
        } else {
            objectToInvoke
        }
    }

    def invokeMethod(def objectToInvoke, final String methodName) {
        invokeMethod(objectToInvoke, methodName, EMPTY_OBJECT_ARRAY)
    }
}

