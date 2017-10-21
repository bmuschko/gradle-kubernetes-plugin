# Release Process

## Overview

The release process uses the [gradle-git](https://github.com/ajoberstar/gradle-git) plugin to 
create a version derived from the current tag and pushes that to master. If no tag is present then 
`0.0.1` will be used as the starting version.

For publishing we use the [build-info-extractor-gradle](https://github.com/JFrogDev/build-info) 
plugin to push our artifacts to Artifactory.

Invoking the `release` task on the root project will execute `clean`, `build`, and 
`bintrayUpload` on each sub-project so it's NOT necessary to put those tasks on 
the command line when releasing.

## Understanding Stages

The [gradle-git](https://github.com/ajoberstar/gradle-git) plugin provides a handful of stages 
one can use but for simplicities sake we will focus only on 2: **SNAPSHOT** and **final**. 

**SNAPSHOT** is the value set by default for the **releaseStage** property. This allows the release process to be 
kicked over and over again, whilst not having to worry about committing changes, and publishes 
artifacts to `libs-snapshot-local`. 

**final** is the value one should use for **releaseStage** property when you're ready to do a production release. 
This will get your artifacts published to `libs-release-local`.

Other stages are available but lets stick to using these 2 if only to keep the process **stupid-simple**.

## Understanding Scopes

The [gradle-git](https://github.com/ajoberstar/gradle-git) plugin provides 3 scopes to be used to 
denote which portion of the `version` (using semver format) to bump.

**major** is the value one should use for **releaseScope** if you want to bump the **major** portion of 
the version. For example: `./gradlew release -PreleaseStage=final -PreleaseScope=major` will turn 
version `1.0.0` to `2.0.0`.

**minor** is the value one should use for **releaseScope** if you want to bump the **minor** portion of 
the version. For example: `./gradlew release -PreleaseStage=final -PreleaseScope=minor` will turn 
version `0.1.0` to `0.2.0`.

**patch** is the value one should use for **releaseScope** if you want to bump the **patch** portion of 
the version. For example: `./gradlew release -PreleaseStage=final -PreleaseScope=patch` will turn 
version `0.0.1` to `0.0.2`.

## Release Steps

### Steps For Development Release

**Development Releases** will have their artifacts published to the `libs-snapshot-local` 
repository with a generated version based off of the latest tag.

1. **Execute**: `./gradlew release` 

### Steps For Production Release

**Production Releases** will have their artifacts published to the `libs-release-local` 
repository with a generated version based off of the latest tag.

1. Ensure `releaseUsername` and `releasePassword` are valid and set within your `~/.gradle/gradle.properties` file.
2. Ensure all changes are committed otherwise an exception from `gradle-git` will be thrown.
3. **Execute**: `./gradlew release -PreleaseStage=final` _// Optional: -PreleaseScope=[major|minor|patch]. Default is `patch`._

## Useful links
* [Semantic Versioning](http://semver.org/)
* [gradle-git version inference](https://github.com/ajoberstar/gradle-git/wiki/Release%20Plugins#version-inference)
