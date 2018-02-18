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

import org.gradle.api.Nullable

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
 *  `response(def)` method.
 *  
 */
trait ResponseAware {

    @Nullable
    private def response // internal and arbitrary response object.
    def response() { response } // public method to get THIS `response` object.
    def responseOn(final def responseToRegister) { // internal method to register an arbitrary response object.
        this.response = responseToRegister
    }
}

