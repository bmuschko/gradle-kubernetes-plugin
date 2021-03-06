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

/**
 *  Enum for all possible RestartPolicy's.
 */
enum RestartPolicy {
    Always,
    OnFailure,
    Never

    /**
     *  Get a RestartPolicy from the passed String. If the passed String is null
     *  then the default kubernetes value of `Always` is returned.
     *  
     *  @param possiblePolicy the `def` to marshal into a RestartPolicy.
     *  @return RestartPolicy the found RestartPolicy.
     */
    static RestartPolicy from(@Nullable def possiblePolicy) {
        if (possiblePolicy == null) {
            return this.values().first()
        } else if (this.isInstance(possiblePolicy)) {
            return possiblePolicy
        } else if (possiblePolicy instanceof Integer) {
            return this.values()[possiblePolicy]
        } else {
            final String localPolicy = possiblePolicy.trim()
            for (final def pol : this.values()) {
                if (pol.name().equalsIgnoreCase(localPolicy)) {
                    return pol
                }
            }
            throw new GradleException("Illegal policy: '${localPolicy}'")  
        }
    }
}

