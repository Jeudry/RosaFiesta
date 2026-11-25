package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.RealtimeStreamEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Multiplatform Server-Sent Events (SSE) client connected to `/v1/stream` for real-time updates.
 */
public class RosaFiestaRealtimeClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val reconnectDelayMs: Long = 3_000L,
    private val maxReconnectDelayMs: Long = 30_000L,
) {
    private val jsonCodec: Json = Json { ignoreUnknownKeys = true }
    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Indica si la suscripción SSE contra `/v1/stream` está actualmente conectada.
     */
    public val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    /**
     * Subscribes to `/v1/stream` using [accessTokenProvider] and emits parsed domain stream events.
     */
    public fun observeStream(
        accessTokenProvider: () -> String?,
    ): Flow<RealtimeStreamEvent> = flow {
        var lastSequenceId = 0L
        var consecutiveFailures = 0
        while (currentCoroutineContext().isActive) {
            val token = accessTokenProvider()?.trim().orEmpty()
            val isLocalOfflineToken = token.startsWith("rf-local.")
            val isMissingToken = token.isEmpty()
            val shouldSkipStream = if (isMissingToken) true else isLocalOfflineToken
            if (shouldSkipStream) {
                _isConnected.value = false
                consecutiveFailures = 0
                delay(reconnectDelayMs)
                continue
            }
            var connectedSuccessfully = false
            try {
                val result = consumeStreamOnce(
                    token = token,
                    lastSequenceId = lastSequenceId,
                    onEvent = { parsedEvent -> emit(parsedEvent) },
                )
                lastSequenceId = result.highestSeq
                connectedSuccessfully = result.connectedSuccessfully
            } catch (cancellation: CancellationException) {
                _isConnected.value = false
                throw cancellation
            } catch (_: Throwable) {
                connectedSuccessfully = false
            } finally {
                _isConnected.value = false
            }
            consecutiveFailures = if (connectedSuccessfully) 0 else (consecutiveFailures + 1).coerceAtMost(10)
            delay(computeBackoffWithJitterMs(consecutiveFailures))
        }
    }

    private fun computeBackoffWithJitterMs(consecutiveFailures: Int): Long {
        val baseMin = reconnectDelayMs.coerceAtLeast(3_000L)
        val ceiling = maxReconnectDelayMs.coerceAtLeast(baseMin)
        if (consecutiveFailures <= 0) {
            return baseMin
        }
        val exponent = (consecutiveFailures - 1).coerceIn(0, 4)
        val exponentialDelay = (baseMin shl exponent).coerceAtMost(ceiling)
        val jitterBound = (exponentialDelay / 4L).coerceAtLeast(1L)
        val jitter = Random.nextLong(from = 0L, until = jitterBound)
        return (exponentialDelay + jitter).coerceIn(baseMin, ceiling)
    }

    private data class StreamConsumeResult(
        val highestSeq: Long,
        val connectedSuccessfully: Boolean,
    )

    private suspend fun consumeStreamOnce(
        token: String,
        lastSequenceId: Long,
        onEvent: suspend (RealtimeStreamEvent) -> Unit,
    ): StreamConsumeResult {
        var highestSeq = lastSequenceId
        var connected = false
        val normalizedBase = baseUrl.trimEnd('/')
        httpClient.prepareGet("$normalizedBase/v1/stream") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "text/event-stream")
            if (highestSeq > 0L) {
                header("Last-Event-ID", highestSeq.toString())
            }
            if (httpClient.pluginOrNull(HttpTimeout) != null) {
                timeout {
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                    socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                }
            }
        }.execute { response ->
            if (!response.status.isSuccess()) {
                _isConnected.value = false
                return@execute
            }
            connected = true
            _isConnected.value = true
            val channel = response.bodyAsChannel()
            highestSeq = readSseFrames(
                channel = channel,
                initialSeq = highestSeq,
                onEvent = onEvent,
            )
            _isConnected.value = false
        }
        return StreamConsumeResult(
            highestSeq = highestSeq,
            connectedSuccessfully = connected,
        )
    }

    private suspend fun readSseFrames(
        channel: ByteReadChannel,
        initialSeq: Long,
        onEvent: suspend (RealtimeStreamEvent) -> Unit,
    ): Long {
        var highestSeq = initialSeq
        var pendingEventName = ""
        var pendingId = ""
        val dataBuffer = StringBuilder()

        while (currentCoroutineContext().isActive) {
            if (channel.isClosedForRead) {
                break
            }
            val line = channel.readLine() ?: break
            if (line.isEmpty()) {
                val parsed = parseBufferedEvent(
                    eventName = pendingEventName,
                    eventId = pendingId,
                    payload = dataBuffer.toString(),
                )
                if (parsed != null) {
                    if (parsed.seq > highestSeq) {
                        highestSeq = parsed.seq
                    }
                    onEvent(parsed)
                }
                pendingEventName = ""
                pendingId = ""
                dataBuffer.clear()
                continue
            }
            if (line.startsWith(":")) {
                continue
            }
            if (line.startsWith("id:")) {
                pendingId = line.removePrefix("id:").trim()
            } else if (line.startsWith("event:")) {
                pendingEventName = line.removePrefix("event:").trim()
            } else if (line.startsWith("data:")) {
                if (dataBuffer.isNotEmpty()) {
                    dataBuffer.append('\n')
                }
                dataBuffer.append(line.removePrefix("data:").trim())
            }
        }
        return highestSeq
    }

    private fun parseBufferedEvent(
        eventName: String,
        eventId: String,
        payload: String,
    ): RealtimeStreamEvent? {
        val cleanPayload = payload.trim()
        if (cleanPayload.isEmpty()) {
            return null
        }
        val dto = runCatching {
            jsonCodec.decodeFromString<RealtimeEventDto>(cleanPayload)
        }.getOrNull()
        if (dto != null) {
            val resolvedSeq = if (dto.seq > 0L) dto.seq else eventId.toLongOrNull() ?: 0L
            return dto.toDomain(fallbackType = eventName, resolvedSeq = resolvedSeq)
        }
        val fallbackType = eventName.ifBlank { "sync.refresh" }
        return RealtimeStreamEvent(
            seq = eventId.toLongOrNull() ?: 0L,
            type = fallbackType,
            entityId = "",
            summary = cleanPayload,
        )
    }
}
