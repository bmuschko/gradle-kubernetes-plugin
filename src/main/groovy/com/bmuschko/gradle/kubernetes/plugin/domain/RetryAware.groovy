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

import net.jodah.failsafe.RetryPolicy
import org.gradle.api.Nullable

/**
 *  Trait that provides a means to configure retries of task execution.
 *
 *  The idea here is that the implementing Task can define a `retry` closure
 *  that will be executed should the internal code execution fail. As working
 *  with remote API's can sometimes fail due to network or resource issues, but
 *  can become available over time, this construct allows for us to deal with
 *  such scenarios in a sane fashion.
 *  
 *  We currently apply this trait to the extension point so that all tasks can
 *  share a common `RetryPolicy` and also on every task so that downstream
 *  implementations can define their own custom `RetryPolicy` should the need
 *  arise. As such task implementations of `RetryPolicy` take precedence over
 *  extension point implementations.
 *  
 *  A typical example when applied to the extension point:
 *
 *      kubernetes {
 *          retry {
 *              withDelay(10, TimeUnit.SECONDS)
 *              withMaxRetries(3)
 *          }
 *      }
 *  
 *  Or when applying to a task:
 *  
 *      task createNamespace(type: CreateNamespace) {
 *          namespace { "namespace-to-create" }
 *          retry {
 *              withDelay(10, TimeUnit.SECONDS)
 *              withMaxRetries(3)
 *          }
 *      }
 */
trait RetryAware {

    @Nullable
    private Closure retry // internal retry Object.
    void retry(final Closure retry) { this.retry = retry } // public method to set retry closure
    def retry() {
        if (retry) {
            final RetryPolicy retryPolicy = new RetryPolicy()
            retry.resolveStrategy = Closure.DELEGATE_FIRST
            retry.delegate = retryPolicy
            retry.call(retryPolicy)
        }
    }
}

