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
 * 
 *   Class for holding Container metadata and to house the most used
 *   parts of working with containers. We're not aiming to provide every
 *   feature creating a container has but simply to implement the most
 *   used ones. Developers looking for every option should consider using
 *   a `POD.yaml` file or even using the `config{}` construct we supply
 *   to get at the underlying `pods` object.
 *   
 */   
trait ContainerSpec {
    final Map<String, InnerContainerSpec> containerSpecs = [:]

    /**
     *  Add a named container to this pod.
     *
     *  @containerName name of container.
     *  @image the image to use to start container.
     *  @envs environment variables to pass to container (optional).
     *  @cmd the cmd to pass into container (optional).
     *  @args the args to expose to container (optional).
     *  @return the InnerContainerSpec instance
     */ 
    InnerContainerSpec addContainer(String containerName,
        String image,
        @Nullable Map<String, String> envs,
        @Nullable List<String> cmd,
        @Nullable List<String> args) {

        final InnerContainerSpec cont = new InnerContainerSpec(image: Objects.requireNonNull(image),
            envs: envs,
            cmd: cmd,
            args: args)

        containerSpecs.put(Objects.requireNonNull(containerName), cont)
        cont
    }

    static class InnerContainerSpec {
        public String image // name of image
        public Map<String, String> envs = [:] // port exposed to pod
        public List<String> cmd = [] // command to use when invoking container
        public List<String> args = [] // args to use to pass to command when invoking container
        public List<VolumeMount> volumeMounts = [] // mounts for container
        public List<Ports> ports = [] // ports for container
        public LivenessProbe livenessProbe // optional liveness probe for container

        /**
         *  Add a named volume mount for container use.
         *
         *  @mountName name of volume mount. This is generally mapped to the name of volume set on the pod.
         *  @mountPath path within container to mount volume.
         */  
        InnerContainerSpec withVolumeMount(String mountName, String mountPath) {
            Objects.requireNonNull(mountName)
            Objects.requireNonNull(mountPath)
            volumeMounts.add(new VolumeMount(name: mountName, mountPath: mountPath))
            this
        }

        /**
         *  Add a container port mapping.
         *
         *  @containerPort port exposed to all containers in pod.
         *  @hostPort port exposed to ALL pods (optional and is generally left null).
         */  
        InnerContainerSpec withPorts(Integer containerPort, @Nullable Integer hostPort) {
            Objects.requireNonNull(containerPort)
            ports.add(new Ports(containerPort: containerPort, hostPort: hostPort))
            this
        }

        /**
         *  Add an exec command probe.
         *
         *  @periodSeconds delay between probe requests.
         *  @initialDelaySeconds delay before first probe kicks.
         *  @timeoutSeconds # of seconds before probe is considered failure.
         *  @command the command on container to execute.
         */  
        InnerContainerSpec withExecProbe(@Nullable Integer periodSeconds,
                                @Nullable Integer initialDelaySeconds,
                                @Nullable Integer timeoutSeconds,
                                List<String> command = []) {
            this.livenessProbe = new LivenessProbe(periodSeconds: periodSeconds,
                                                    initialDelaySeconds: initialDelaySeconds,
                                                    timeoutSeconds: timeoutSeconds,
                                                    probeType: new ExecProbe(command: command))
            this
        }

        /**
         *  Add an http probe.
         *
         *  @periodSeconds delay between probe requests.
         *  @initialDelaySeconds delay before first probe kicks.
         *  @timeoutSeconds # of seconds before probe is considered failure.
         *  @path the http path to query.
         *  @port the port to query.
         *  @headers the optional headers to pass.
         */  
        InnerContainerSpec withHttpProbe(@Nullable Integer periodSeconds,
                                @Nullable Integer initialDelaySeconds,
                                @Nullable Integer timeoutSeconds,
                                String path,
                                @Nullable Integer port,
                                @Nullable Map<String, String> headers = [:]) {
            this.livenessProbe = new LivenessProbe(periodSeconds: periodSeconds,
                                                    initialDelaySeconds: initialDelaySeconds,
                                                    timeoutSeconds: timeoutSeconds,
                                                    probeType: new HttpProbe(path: path, port: port, headers: headers))
            this
        }

        // VolumeMount for container
        static class VolumeMount {
            public String name // volume mount name
            public String mountPath // path to mount
        }

        // port mappings for container
        static class Ports {
            public Integer containerPort // port exposed to pod
            public Integer hostPort // port exposed to ALL pods (generally 
        }

        // liveness probe for container
        static class LivenessProbe {
            public Integer periodSeconds // perform probe every X number of seconds
            public Integer initialDelaySeconds // initial delay before performing first probe
            public Integer timeoutSeconds // # of seconds before we timeout and fail
            def probeType
        }

        // probe for executing command on container
        static class ExecProbe {
            public List<String> command = []
        }

        // probe for performing http requests against container
        static class HttpProbe {
            public String path
            public Integer port
            public Map<String, String> headers = []
        }
    }
}

