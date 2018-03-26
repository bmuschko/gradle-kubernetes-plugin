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

package com.bmuschko.gradle.kubernetes.plugin.common

import java.util.concurrent.atomic.AtomicReference

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
     * Invoke no-arg method on Object with supplied arguments. If
     * args is null then `objectToInvoke` is simply returned.
     *
     * The AtomicReference is updated with the potentially new value coming
     * out of the invoked method.
     *
     * @param objectToInvoke AtomicReference holding object to invoke method on
     * @param methodName name of method to find and invoke
     * @param args arguments to pass to potential method
     * @return the output, or potentially new Object, created from calling the method
     */
    static def invokeMethod(final AtomicReference<Object> objectToInvoke, final String methodName, final Object... args) {
        if (args != null) {
            def localObject = objectToInvoke.get()
            def metaMethod = localObject.metaClass.getMetaMethod(methodName, args)
            if (metaMethod) {
                def localResponse = metaMethod.invoke(localObject, args)
                objectToInvoke.set(localResponse)
            } else {
                throw new GradleException("Cannot invoke method '${methodName}' on class '${objectToInvoke.class}'. Was it previously set?")
            }
        }
        objectToInvoke
    }

    /**
     * Invoke no-arg method on Object.
     *
     * The AtomicReference is updated with the potentially new value coming
     * out of the invoked method.
     *
     * @param objectToInvoke AtomicReference holding object to invoke method on
     * @param methodName name of method to find and invoke
     * @return the output, or potentially new Object, created from calling the method
     */
    static def invokeMethod(final AtomicReference<Object> objectToInvoke, final String methodName) {
        invokeMethod(objectToInvoke, methodName, EMPTY_OBJECT_ARRAY)
    }

    /**
     * Wrap an arbitrary Object in an AtomicReference
     */
    static def wrapAtomic(Object obj) {
        new AtomicReference<Object>(obj)
    }
}
