# BaseMind.AI Android SDK

This repository hosts the BaseMind.AI Android SDK. The SDK is a gRPC client library for connecting with the
BaseMind.AI platform.

## Local Development

<u>Repository Structure:</u>

```text
root                        # repository root, holding all tooling configurations
├─── .github                # GitHub CI/CD and other configurations
├─── .idea                  # IDE configurations that are shared
├─── proto/gateway          # Git submodule that includes the protobuf schema
├─── gradle                 # the version catalog and the gradle wrapper
├─── example-app            # example working demonstrating usage of the SDK
└─── sdk                    # the Android SDK code
```

### Installation

1. Clone to repository to your local machine including the submodules.

```shell
git clone --recurse-submodules https://github.com/basemind-ai/sdk-android.git
```

2. Install [TaskFile](https://taskfile.dev/) and the following prerequisites:

    - Python >= 3.11
    - Java >= 17.0
    - Android Studio or IntelliJ (optional but recommended)

3. Execute the setup task with:

```shell
task setup
```

This will setup [pre-commit](https://pre-commit.com/).

### Linting

To lint the project, execute the lint command:

```shell
task lint
```

### Updating Dependencies

To update the dependencies, execute the update-dependencies command:

```shell
task update
```

This will update the dependencies in the [version catalog file](./gradle/libs.versions.toml). It will also update
the pre-commit hooks.

The versions of the `protoc` and the related `protobuf` and `grpc` tooling declared in
[the SDK build.gradle file](./sdk/build.gradle.kts), must be updated manually.

### Executing Tests

The project comes with a set of run configurations for IntelliJ and Android Studio. You can execute them from inside the
IDE.

Otherwise, you can execute any command using `gradlew`, for example:

```shell
./gradlew test
```

## Contribution

The SDK is open source.

Pull requests are welcome - as long as they address an issue or substantially improve the SDK or the test app.

Note: Tests are mandatory for the SDK - untested code will not be merged.
