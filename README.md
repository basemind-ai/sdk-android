# BaseMind.AI Android SDK

<div align="center">

[![Discord](https://img.shields.io/discord/1153195687459160197)](https://discord.gg/ZSV2CQ86yg)

</div>

The BaseMind.AI Android SDK is a gRPC client library for connecting with the BaseMind.AI platform.

## Installation

Add the dependency in your application's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("ai.basemind:client:1.0.0")
}
```

## Usage

Before using the client you have to initialize it. The init function requires an `apiKey` that you can create using the
BaseMind platform (visit https://basemind.ai):

```kotlin
import ai.basemind.client.BaseMindClient

val client = BaseMindClient.getInstance("<API_KEY")
```

Once the client is initialized, you can use it to interact with the AI model(s) you configured in the BaseMind dashboard.

### Prompt Request/Response

You can request an LLM prompt using the `requestPrompt` method, which expects a dictionary of string key/value pairs -
correlating with any template variables defined in the dashboard (if any):

```kotlin
import ai.basemind.client.BaseMindClient

val client = BaseMindClient.getInstance("<API_KEY")

fun handlePromptRequest(userInput: String): String {
    val map = mapOf("userInput" to userInput)
    val response = client.requestPrompt(map)

    return response.content
}
```

### Prompt Streaming

You can also stream a prompt response using the `requestStream` method:

```kotlin
import ai.basemind.client.BaseMindClient

val client = BaseMindClient.getInstance("<API_KEY")

fun handlePromptStream(userInput: String): MutableList<String> {
    val map = mapOf("userInput" to userInput)
    val response = client.requestStream(map)

    val chunks: MutableList<String> = mutableListOf()
    response.collect { chunk -> chunks.add(chunk.content) }

    return chunks
}
```

Similarly to the `requestPrompt` method, `requestStream` expects a mapping of strings (if any template variables are
defined in the dashboard).

### Error Handling

All errors thrown by the client are subclasses of `BaseMindException`. Errors are thrown in the following cases:

1. The api key is empty (`MissingAPIKeyException`).
2. A server side or connectivity error occurred (`APIGatewayException`)
3. A required template variable was not provided in the mapping passed to the request (`MissingPromptVariableException`).

### Options

You can pass an options object to the client:

```kotlin
import ai.basemind.client.BaseMindClient
import ai.basemind.client.Options

val options = Options(
    debug = true,
    debugLogger = { tag, message -> Log.i(tag, message)},
    serverAddress = "127.0.0.1",
    serverPort = 443,
    terminationDelaySeconds = 10L,
)

val client = BaseMindClient.getInstance("<API_KEY", options = options)
```

-   `debugLogger`: a function that receives a logging tag and a logging message and handles logging. Defaults to using `Log.d`.
-   `debug`: if set to true (default false) the client will log debug messages.
-   `serverAddress`: the host of the BaseMind Gateway server to use.
-   `serverPort`: the server port.
-   `terminationDelaySeconds`: The amount of seconds a channel shutdown should wait before force terminating requests. Defaults to 5 seconds.

### Passing Prompt Config ID

The `BaseMindClient.getInstance` also accepts an optional `promptConfigId` parameter. This parameter is `null` by default

-   which means the client will use the prompt configuration defined as `default` in the dashboard. You
    can also pass a specific prompt config ID to the client:

```kotlin
import ai.basemind.client.BaseMindClient

val client = BaseMindClient.getInstance("<API_KEY", promptConfigId = "c5f5d1fd-d25d-4ba2-b103-8c85f48a679d")
```

**Note**: you can have multiple client instances with different `promptConfigId` values set. This allows you to use
multiple model configurations within a single application.

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

### Setup

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
