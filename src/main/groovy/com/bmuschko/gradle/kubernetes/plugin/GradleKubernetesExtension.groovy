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

import com.bmuschko.gradle.kubernetes.plugin.domain.ConfigureAware

/**
 *  Extension point for the gradle-kubernetes-plugin. Currently this
 *  class serves only to provide a mapping to the `Config` instance
 *  of the `KubernetesClient`. Instead of defining a thousand properties
 *  here, which are already defined elsewhere, we can instead do
 *  something like:
 *  
 *      kubernetes {
 *          config {
 *              withMasterUrl("https://mymaster.com")
 *          }
 *      }
 *
 *  Through the use of an annotation builder all setter methods of the
 *  `Config` class can optionally use `with*` versions as well.
 *
 *
 *  Class provides a direct mapping to the `Config` class @
 *
 *  @see <a href="https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/Config.java">Config</a>
 */
class GradleKubernetesExtension implements ConfigureAware {

}
