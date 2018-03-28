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

package com.bmuschko.gradle.kubernetes.plugin.domain.services

import static com.bmuschko.gradle.kubernetes.plugin.GradleKubernetesUtils.randomString

import org.gradle.api.Nullable
import org.gradle.api.tasks.Internal

/**
 *
 * Trait for holding port mappings typically used by a Service.
 *
 */
trait PortSpec {

    // marking this as internal, and then creating a method to access it,
    // is mainly done to get around various gradle "syntax validation checks".
    @Internal
    private final List<InnerPortSpec> portSpecs = []
    public List<InnerPortSpec> portSpecs() {
        portSpecs
    }

    /**
     *  Add ports for this service to expose.
     *
     *  @param nodePort the nodePort to expose.
     *  @param podPort the podPort (host) to expose.
     *  @param targetPort the targetPort (container) to expose.
     *  @return PortSpec to further add ports if requested.
     */
    public PortSpec addPorts(@Nullable Integer nodePort,
            @Nullable Integer podPort,
            @Nullable Integer targetPort) {
        addPorts(null, null, nodePort, podPort, targetPort)
    }

    /**
     *  Add additional ports to this spec.
     *
     *  @param name the optional name of the port mapping. // generated if not set
     *  @param protocol the optional protocol to specify. // set to 'TCP' if not set
     *  @param nodePort the nodePort to expose.
     *  @param podPort the podPort (host) to expose.
     *  @param targetPort the targetPort (container) to expose.
     *  @return PortSpec to further add ports if requested.
     */
    PortSpec addPorts(@Nullable String name,
        @Nullable String protocol,
        @Nullable Integer nodePort,
        @Nullable Integer podPort,
        @Nullable Integer targetPort) {
        portSpecs << new InnerPortSpec(name: name ?: randomString(null),
            protocol: protocol,
            nodePort: nodePort,
            podPort: podPort,
            targetPort: targetPort)
        this
    }

    static class InnerPortSpec {
        public String name
        public String protocol // UDP, TCP, etc
        public Integer nodePort // port exposed to world
        public Integer podPort // port exposed to all pods
        public Integer targetPort // port exposed from container
    }
}


