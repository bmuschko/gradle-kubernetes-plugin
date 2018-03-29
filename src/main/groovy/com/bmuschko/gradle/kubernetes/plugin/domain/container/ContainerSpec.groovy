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

package com.bmuschko.gradle.kubernetes.plugin.domain.container

import org.gradle.api.GradleException
import org.gradle.api.Nullable
import org.gradle.api.tasks.Internal

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

    public static final enum PROBE_TYPES { liveness, readiness }

    // marking this as internal, and then creating a method to access it,
    // is mainly done to get around various gradle "syntax validation checks".
    @Internal
    private final Map<String, InnerContainerSpec> containerSpecs = [:]
    public Map<String, InnerContainerSpec> containerSpecs() {
        containerSpecs
    }

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
        public Probe livenessProbe // optional liveness probe for container
        public Probe readinessProbe // optional readiness probe for container
        public TerminationMessage terminationMessage // optional termination message options

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
         *  Add an exec command liveness probe.
         *
         *  @type can be either 'liveness' or 'readiness' (default to former)
         *  @periodSeconds delay between probe requests.
         *  @initialDelaySeconds delay before first probe kicks.
         *  @timeoutSeconds # of seconds before probe is considered failure.
         *  @command the command on container to execute.
         */  
        InnerContainerSpec withExecProbe(@Nullable String type = 'liveness',
                                @Nullable Integer periodSeconds,
                                @Nullable Integer initialDelaySeconds,
                                @Nullable Integer timeoutSeconds,
                                List<String> command = []) {
            Probe localProbe = new Probe(periodSeconds: periodSeconds,
                                        initialDelaySeconds: initialDelaySeconds,
                                        timeoutSeconds: timeoutSeconds,
                                        probeInstance: new ExecProbe(command: command))

            // set proper probe
            switch (type.trim().toLowerCase()) {
                case 'liveness': livenessProbe = localProbe; break;
                case 'readiness': readinessProbe = localProbe; break;
                default: throw new GradleException("Unknown probe type: ${type}")
            }
            this
        }

        /**
         *  Add an http liveness probe.
         *
         *  @type can be either 'liveness' or 'readiness' (default to former)
         *  @periodSeconds delay between probe requests.
         *  @initialDelaySeconds delay before first probe kicks.
         *  @timeoutSeconds # of seconds before probe is considered failure.
         *  @path the http path to query.
         *  @port the port to query.
         *  @headers the optional headers to pass.
         */  
        InnerContainerSpec withHttpProbe(@Nullable String type = 'liveness',
                                @Nullable Integer periodSeconds,
                                @Nullable Integer initialDelaySeconds,
                                @Nullable Integer timeoutSeconds,
                                String path,
                                @Nullable Integer port,
                                @Nullable Map<String, String> headers = [:]) {
            Probe localProbe = new Probe(periodSeconds: periodSeconds,
                                        initialDelaySeconds: initialDelaySeconds,
                                        timeoutSeconds: timeoutSeconds,
                                        probeInstance: new HttpProbe(path: path, port: port, headers: headers))
            // set proper probe
            switch (type.trim().toLowerCase()) {
                case 'liveness': livenessProbe = localProbe; break;
                case 'readiness': readinessProbe = localProbe; break;
                default: throw new GradleException("Unknown probe type: ${type}")
            }
            this
        }

        /**
         *  Set the termination policy and message path.
         *
         *  @param policy the termination message policy (defaults to `File`)
         *  @param path the termination message path (default to `/dev/termination-log`)
         */
        InnerContainerSpec withTerminationMessage(@Nullable String policy, @Nullable String path) {
            final TerminationMessagePolicy localPolicy = TerminationMessagePolicy.from(policy).toString()
            terminationMessage = new TerminationMessage(policy: localPolicy.toString(), path: path)
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

        // probe for container
        static class Probe {
            public Integer periodSeconds // perform probe every X number of seconds
            public Integer initialDelaySeconds // initial delay before performing first probe
            public Integer timeoutSeconds // # of seconds before we timeout and fail
            public def probeInstance // arbitrary probe that must be inferred at runtime
        }

        // termination message and policy for container
        static class TerminationMessage {
            String policy
            String path
        }
    }
}

