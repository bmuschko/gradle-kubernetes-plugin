### Version 0.9.1 (April 11, 2018)
* Added task `ListDeployments` - [Pull request 21](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/21)
* Added task `DeleteDeployments` - [Pull request 22](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/22)
* Added task `GetDeployments` - [Pull request 23](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/23)
* Added task `CreateDeployments` - [Pull request 24](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/24)

### Version 0.9.0 (March 31, 2018)
* Added task `ListPods` - [Pull request 16](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/16)
* Added task `DeletePod` - [Pull request 17](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/17)
* Added task `GetPod` - [Pull request 18](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/18)
* Added task `CreatePod` - [Pull request 20](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/20)

### Version 0.0.4 (March 16, 2018)
* Added task `ListServices` - [Pull request 10](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/10)
* Added task `GetService` - [Pull request 11](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/11)
* Added task `DeleteService` - [Pull request 12](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/12)
* Added task `CreateService` - [Pull request 13](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/13)
* Bump `kubernetes-client` to `3.1.10` - [Pull request 14](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/14)

### Version 0.0.3 (March 14, 2018)
* Added task `KubernetesClient` which returns a raw `kubernetes-client` instance to use.
* All existing tasks have been refactored to prefer traditional property inputs over using Closures.
* `GradleKubernetesExtension` gained property `useOpenShiftAdapter` to switch to using the _open-shift_ wrapper in favor or the default _kubernetes_ wrapper.

### Version 0.0.1 (March 10, 2018)
* Added task `ListNamespaces` - [Commit 331083a](https://github.com/bmuschko/gradle-kubernetes-plugin/commit/331083a)
* Added task `GetNamspace` - [Commit 772ce86](https://github.com/bmuschko/gradle-kubernetes-plugin/commit/772ce86)
* Added task `DeleteNamspace` - [Commit 2756f3e](https://github.com/bmuschko/gradle-kubernetes-plugin/commit/2756f3e)
* Added task `CreateNamspace` - [Commit fa67d39](https://github.com/bmuschko/gradle-kubernetes-plugin/commit/fa67d39)
