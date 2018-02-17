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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 *
 *  Base class for all functional tests.
 *
 */
abstract class AbstractFunctionalTest extends Specification {

    public static final String RESPONSE_SET_MESSAGE = 'Response object was properly set'
    public static final String ON_ERROR_NOT_REACHED = 'onError: we should NOT reach here'
    public static final String ON_NEXT_REACHED = 'onNext: output is NOT null'
    public static final String ON_COMPLETE_REACHED = 'onComplete: we are done'

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File projectDir
    File buildFile

    // basic check to ensure we can get off the ground.
    def setup() {
        projectDir = temporaryFolder.root
        setupBuildfile()

        when:
            BuildResult result = build('kubernetesConfig')

        then:
            result.output.contains('Api-Version: ')
            result.output.contains('Master-URL: ')
    }

    protected void setupBuildfile() {
        if (buildFile) {
            buildFile.delete()
        }
        buildFile = temporaryFolder.newFile('build.gradle')

        buildFile << """
            plugins {
                id 'gradle-kubernetes-plugin'
            }

            repositories {
                jcenter()
            }
        """

        buildFile << """
            task kubernetesConfig(type: com.bmuschko.gradle.kubernetes.plugin.tasks.system.Configuration)
        """
    }

    protected BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }

    protected BuildResult buildAndFail(String... arguments) {
        createAndConfigureGradleRunner(arguments).buildAndFail()
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        def args = ['--stacktrace']
        if (arguments) {
            args.addAll(arguments)
        }
        GradleRunner.create().withProjectDir(projectDir).withArguments(args).withPluginClasspath()
    }
}

