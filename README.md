# BaseMind.AI Android SDK

This repository hosts the BaseMind.AI Android SDK. The SDK is a gRPC client library for connecting with the
BaseMind.AI platform.

```text
root                        # repository root, holding all tooling configurations
├─── .github                # GitHub CI/CD and other configurations
├─── .idea                  # IDE configurations that are shared
├─── test-app     # test application to test the SDK
└─── sdk          # the Android SDK code
```

## Installation

1. Install [TaskFile](https://taskfile.dev/) and the following prerequisites:

    - Python >= 3.11
    - Java >= 17.0

2. Execute the setup task with:

```shell
task setup
```

## Linting

We use [TaskFile](https://taskfile.dev/) to orchestrate tooling and commands.
To lint the project execute:

```shell
task lint
```

## Contribution

The SDK is open source.

Pull requests are welcome - as long as they address an issue or substantially improve the SDK or the test app.
