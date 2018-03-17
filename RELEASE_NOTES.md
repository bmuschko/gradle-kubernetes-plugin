### Version 0.9.0 (TBA)
* Added task `ListPods` - [Pull request 16](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/16)
* Added task `DeletePod` - [Pull request 17](https://github.com/bmuschko/gradle-kubernetes-plugin/pull/17)

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
* Initial release with CRUD operations for kubernetes _namespace_
