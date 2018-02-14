# gradle-kubernetes-plugin

Gradle plugin for working with Kubernetes.

## Project(s) Status

| Questions | CI | Codecov | Docs | Release |
| :---: | :---: | :---: | :---: | :---: |
| [![Stack Overflow](https://img.shields.io/badge/stack-overflow-4183C4.svg)](https://stackoverflow.com/questions/tagged/gradle-kubernetes-plugin) | [![Build Status](https://travis-ci.org/bmuschko/gradle-kubernetes-plugin.svg?branch=master)](https://travis-ci.org/bmuschko/gradle-kubernetes-plugin) | [![codecov](https://codecov.io/gh/bmuschko/gradle-kubernetes-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/bmuschko/gradle-kubernetes-plugin) | [![Docs](https://img.shields.io/badge/docs-latest-blue.svg)](http://htmlpreview.github.io/?https://github.com/bmuschko/gradle-kubernetes-plugin/blob/gh-pages/docs/index.html) | [![gradle-kubernetes-plugin](https://api.bintray.com/packages/bmuschko/libs-release-local/gradle-kubernetes-plugin/images/download.svg) ](https://bintray.com/bmuschko/libs-release-local/gradle-kubernetes-plugin/_latestVersion) |

## Latest Release

Can be sourced from JCenter/Artifactory like so:
```
<dependency>
    <groupId>com.bmuschko</groupId>
    <artifactId>gradle-kubernetes-plugin</artifactId>
    <version>X.Y.Z</version>
    <classifier>sources|tests|docs</classifier> (Optional)
</dependency>
```

## Adding New Projects

For _stand-alone projects_ you can create the typical `src/main/<language>` directory
at the root of this project and things will work as expected. In this model it is OK
to remove the `projects` directory once you have things in place.

For _multi projects_ you need to create/place said project(s) under the `projects` directory. 
Placing the project(s) here will have them built automatically as part of this multi-project 
build.

For either case you may have to twiddle the knobs a bit within the `gradle/projects.gradle` 
file depending on which type of project you want to create.

## Project Structure

The package structure of your project should begin with your gradle `group` followed by the projects name. As
part of building your project we will scan through its sources and ensure this pattern is met. For example: if 
your group is `com.github.gradle` and you're adding a project named `calamari` then the package structure of 
that project must look like `com/github/gradle/calamari`. 

If you're adding a project whose name contains non-alphabetic characters then those characters will be converted to forward-slashes when running the previously mentioned check. For example: if your project is named `tuna-casserole` then your package structure would look like `com/github/gradle/tuna/casserole.

## Jacoco, ErrorProne, Checkstyle, PMD, and FindBugs support

**[Jacoco](https://github.com/jacoco/jacoco)**: is a tool that ensures new code has 
proper test coverage.

**[ErrorProne](https://github.com/google/error-prone)**: is a static analysis tool 
for Java that catches common programming mistakes at compile-time and suggests fixes. 

**[Checkstyle](https://github.com/checkstyle/checkstyle)**: is a development tool that 
forces programmers to write code that adheres to a common standard.

**[PMD](https://github.com/pmd/pmd)**: is a source code analyzer that finds common programming 
flaws like unused variables, empty catch blocks, unnecessary object creation, and so forth.

**[FindBugs](https://github.com/findbugsproject/findbugs)**: is a tool which uses static 
analysis to look for and detect possible bugs in Java code.

## Use Of Test Libraries and Writing Tests

Currently we define `junit` and `assertj` as `testCompile` dependencies for all projects 
to use. Lets try to focus on using just these, and if there is a need to bring in and 
use something else, then lets first have a discussion on it before we go adding N 
number of dependencies to this project and break the look and feel we are trying to set.

Code is considered done-done when all checks have passed, code can be compiled, and at the 
very least unit and integration tests have been added to address the new code.

## Additional Resources

* [Release Process](https://github.com/bmuschko/gradle-kubernetes-plugin/blob/master/docs/RELEASE_PROCESS.md)

