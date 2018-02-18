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

import org.gradle.api.Nullable
import org.gradle.util.ConfigureUtil

/**
 *  Trait that provides a generic `response` API.
 *
 *  The idea is to provide a common means of setting an arbitrary
 *  response/result object for all tasks so that downstream tasks, be
 *  they part of this plugin or something external, can query in a
 *  previously executed task to get its response and potentially
 *  work with it in some way.
 *
 *  For example:
 *
 *      task getAllNamespaces(type: ListNamespaces) {
 *          config {
 *              setSomeConfigProperty("HelloWorld")
 *          }
 *      }
 *
 *      task workflow(dependsOn: getAllNamespaces) {
 *          doLast {
 *              // get namespaces object and print metadata
 *              def namespacesObject = getAllNamespaces.response()
 *              namespacesObject.getMetadata().getAdditionalProperties().each { k,v ->
 *                  logger.quiet "Found key=${k}, value=${v}"
 *              }
 *          }
 *      }
 *
 *  For tasks that implement this trait it is THEIR RESPONSIBILITY to call,
 *  possibly as the last line of code in their `handleClient` impl, the
 *  `registerResponse(object)` method.
 *  
 */
trait ResponseAware {

    /**
     * Response object, possibly NULL, returned from the execution.
     */
    @Nullable
    private def response

    /**
     *  Response, possibly null, returned from execution. This is an attempt
     *  at creating a generic way all tasks of this plugin return data. The
     *  data itself can be anything and is not restricted by any rules imposed
     *  by our super-class `AbstractReactiveStreamsTask`. This CAN be the data
     *  returned from `runRemoteCommand` but does not necessarily have to be.
     *
     *  Internal tasks should take careful care to invoke the `registerResponse(def)`
     *  method, generally as the last line of execution, to give external downstream tasks
     *  (i.e. tasks/code not from this plugin) something to work with.
     */
    def response() {
        response
    }

    /**
     *  Internal helper method for all tasks of this plugin to explicitly
     *  register a response object for external downstream use.
     */
    def registerResponse(final def responseToRegister) {
        this.response = responseToRegister
    }
}

