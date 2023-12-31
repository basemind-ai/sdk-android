package ai.basemind.client

import ai.basemind.grpc.APIGatewayServiceGrpcKt
import ai.basemind.grpc.PromptRequest
import ai.basemind.grpc.PromptResponse
import ai.basemind.grpc.StreamingPromptResponse
import android.util.Log
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.Closeable
import java.util.concurrent.TimeUnit

internal const val DEFAULT_API_GATEWAY_ADDRESS = "gateway.basemind.ai"
internal const val DEFAULT_API_GATEWAY_PORT = 443
internal const val DEFAULT_TERMINATION_DELAY_S = 5L
internal const val LOGGING_TAG = "BaseMindClient"

@DslMarker
private annotation class BasemindClientDsl

/**
 * BaseMindClient Options
 *
 * This dataclass is used for configuring the client.
 */
data class Options(
    /**
     * The amount of seconds a channel shutdown should wait before force terminating requests.
     *
     * Defaults to 5 seconds.
     */
    val terminationDelaySeconds: Long = DEFAULT_TERMINATION_DELAY_S,
    /**
     * Controls outputting debug log messages. Defaults to 'false'.
     */
    val debug: Boolean = false,
    /**
     * A logging handler function. Defaults to android's Log.d.
     */
    val debugLogger: (String, String) -> Unit = { tag, message -> Log.d(tag, message) },
    /**
     * The address of the basemind gateway to connect to.
     */
    val serverAddress: String = DEFAULT_API_GATEWAY_ADDRESS,
    /**
     * The server port to use.
     */
    val serverPort: Int = DEFAULT_API_GATEWAY_PORT,
)

/**
 * BaseMindClient is an API client that uses gRPC for communication with the BaseMind.AI API gateway.
 * @property apiToken the API token to use for authentication. This parameter is required.
 * @property options an options object. This parameter is optional.
 * @constructor instantiates a new client instance.
 */
class BaseMindClient private constructor(
    private val apiToken: String,
    private val promptConfigId: String?,
    private val options: Options,
) : Closeable {
    init {
        if (apiToken.isEmpty()) {
            throw MissingAPIKeyException()
        }
    }

    private val channel =
        run {
            if (options.debug) {
                val message = "Connecting to ${options.serverAddress}:${options.serverPort}"
                options.debugLogger(LOGGING_TAG, message)
            }

            /**
             * creates a gRPC channel builder instance, which allows us to set options
             */
            ManagedChannelBuilder
                .forAddress(options.serverAddress, options.serverPort)
                .useTransportSecurity() // we use TLS
                .executor(Dispatchers.IO.asExecutor()) // we use the IO dispatcher for gRPC
                .build()
        }

    internal var grpcStub = APIGatewayServiceGrpcKt.APIGatewayServiceCoroutineStub(channel)

    companion object {
        private val instances = mutableMapOf<Int, BaseMindClient>()

        /**
         * Creates a new client instance.
         *
         * @param apiToken the API token to use for authentication. This parameter is required.
         * @param promptConfigId the prompt config id to use for the client. This parameter is optional.
         * @param options an options object. This parameter is optional.
         */
        @BasemindClientDsl
        fun getInstance(
            apiToken: String,
            promptConfigId: String? = null,
            options: Options = Options(),
        ): BaseMindClient {
            if (options.debug) {
                options.debugLogger(LOGGING_TAG, "creating client instance")
            }

            val key = options.hashCode() + apiToken.hashCode() + promptConfigId.hashCode()

            if (!instances.containsKey(key)) {
                instances[key] = BaseMindClient(apiToken = apiToken, promptConfigId = promptConfigId, options = options)
            }

            return instances[key]!!
        }
    }

    /**
     * closes the connection to the API gateway.
     *
     * Note: Existing calls are allowed to finish, but all new calls will be declined after close is called.
     */
    override fun close() {
        channel.shutdown().awaitTermination(options.terminationDelaySeconds, TimeUnit.SECONDS)
    }

    private fun createPromptRequest(templateVariables: Map<String, String>): PromptRequest {
        val configId = promptConfigId

        return PromptRequest.newBuilder().apply {
            templateVariables.forEach { (key, value) ->
                putTemplateVariables(key, value)
            }
            if (configId != null) {
                this.promptConfigId = configId
            }
        }.build()
    }

    private fun createMetadata(): Metadata {
        val metadata = Metadata()
        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer $apiToken")
        return metadata
    }

    /**
     * Requests an AI prompt. The prompt is returned as a single response.
     *
     * @param templateVariables a map of template variables to use for the prompt request.
     * @throws MissingPromptVariableException if a template variable is missing.
     * @throws APIGatewayException if the API gateway returns an error.
     */
    suspend fun requestPrompt(templateVariables: Map<String, String>): PromptResponse {
        try {
            if (options.debug) {
                options.debugLogger(LOGGING_TAG, "requesting prompt")
            }

            return grpcStub.requestPrompt(createPromptRequest(templateVariables), createMetadata())
        } catch (e: StatusException) {
            if (options.debug) {
                options.debugLogger(LOGGING_TAG, "exception requesting prompt: $e")
            }

            when (e.status.code) {
                io.grpc.Status.Code.INVALID_ARGUMENT -> throw MissingPromptVariableException(
                    e.message ?: "Missing prompt variable",
                    e,
                )

                else -> throw APIGatewayException(e.message ?: "API Gateway error", e)
            }
        }
    }

    /**
     * Requests an AI streaming prompt. The prompt is streamed from the API gateway in chunks.
     *
     * @param templateVariables a map of template variables to use for the prompt request.
     * @throws MissingPromptVariableException if a template variable is missing.
     * @throws APIGatewayException if the API gateway returns an error.
     */
    fun requestStream(templateVariables: Map<String, String>): Flow<StreamingPromptResponse> {
        if (options.debug) {
            options.debugLogger(LOGGING_TAG, "requesting streaming prompt")
        }

        val response = grpcStub.requestStreamingPrompt(createPromptRequest(templateVariables), createMetadata())
            .catch { e ->
                if (options.debug) {
                    options.debugLogger(LOGGING_TAG, "exception requesting streaming prompt: $e")
                }

                if (e !is StatusException) {
                    throw e // skipcq: TCV-001
                }

                when (e.status.code) {
                    io.grpc.Status.Code.INVALID_ARGUMENT -> throw MissingPromptVariableException(
                        e.message ?: "Missing prompt variable",
                        e,
                    )

                    else -> throw APIGatewayException(e.message ?: "API Gateway error", e)
                }
            }
        return response
    }
}
