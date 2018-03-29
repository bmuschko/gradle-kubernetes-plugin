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
 *  Enum for all possible termination message policies
 */
enum TerminationMessagePolicy {
    File,
    FallbackToLogsOnError

    /**
     *  Get a TerminationMessagePolicy from the passed String. If the passed String is null
     *  then the default kubernetes value of `File` is returned.
     *  
     *  @param possiblePolicy the String to marshal into a TerminationMessagePolicy.
     *  @return TerminationMessagePolicy the found RestartPolicy.
     */
    static TerminationMessagePolicy from(@Nullable String possiblePolicy) {
        if (possiblePolicy) {
            final String localPolicy = possiblePolicy.trim()
            for (final TerminationMessagePolicy pol : this.values()) {
                if (pol.name().equalsIgnoreCase(localPolicy)) {
                    return pol
                }
            }
            throw new GradleException("Illegal policy: '${localPolicy}'")
        } else {
            return File
        }
    }
}

