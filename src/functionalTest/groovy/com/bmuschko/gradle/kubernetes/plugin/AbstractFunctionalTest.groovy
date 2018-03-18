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
    public static final String SHOULD_NOT_REACH_HERE = "NOT A GOOD PLACE TO BE"
    public static final String SHOULD_REACH_HERE = "WE ARE HERE"

    final String possibleEndpoint = System.getProperty('test.kubernetes.endpoint')
    final String possibleUsername = System.getProperty('test.kubernetes.username')
    final String possiblePassword = System.getProperty('test.kubernetes.password')
    final String possibleOffline = System.getProperty('test.kubernetes.offline')

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File defaultPodFile = new File(loadResource('/pods/nginx-pod.yaml').getFile())
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
                mavenLocal()
                jcenter()
            }
        """
        if (possibleEndpoint) {
            buildFile << """
                kubernetes { config { withMasterUrl '$possibleEndpoint' } }
            """  
        }
        if (possibleUsername) {
            buildFile << """
                kubernetes { config { withUsername '$possibleUsername' } }
            """  
        }
        if (possiblePassword) {
            buildFile << """
                kubernetes { config { withPassword '$possiblePassword' } }
            """  
        }

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
        def args = ['--stacktrace',
            '-Dorg.gradle.daemon=false',
            '-Dorg.gradle.jvmargs=-XX:+CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC -XX:+HeapDumpOnOutOfMemoryError']
        if (Boolean.valueOf(possibleOffline).booleanValue() == true) {
            args << '--offline'
        }
        if (arguments) {
            args.addAll(arguments)
        }
        GradleRunner.create().withProjectDir(projectDir).withArguments(args).withPluginClasspath()
    }

    public static String randomString() {
        'gkp-' + UUID.randomUUID().toString().replaceAll("-", "")
    }

    /**
     * Count the number of instances of substring within a string.
     *
     * @param string     String to look for substring in.
     * @param substring  Sub-string to look for.
     * @return           Count of substrings in string.
     */
    public static int count(final String string, final String substring) {
       int count = 0;
       int idx = 0;

       while ((idx = string.indexOf(substring, idx)) != -1) {
          idx++;
          count++;
       }

       return count;
    }

    // load an arbitrary file from classpath resource
    public static URL loadResource(String resourcePath) {
        this.getClass().getResource(resourcePath)
    }

    /**
     * Copy and replace an arbitrary number of tokens in a given file. If no
     * tokens are found then file is more/less just copied to new destination.
     *
     * @param source the source file we will replace tokens in
     * @param destination the destination file we will write
     * @param tokensToReplaceWithValues map where key=token-to-replace, value=value-to-replace-with
     */
    public static void copyAndReplaceTokensInFile(File source, File destination, def tokensToReplaceWithValues = [:]) {
        destination.withWriter { dest ->
            source.eachLine { line ->
                def localLine = line
                tokensToReplaceWithValues.each { k, v ->
                    localLine = localLine.replaceAll(k, String.valueOf(v))
                }
                dest << localLine + System.getProperty('line.separator')
            }
        }
    }
}
