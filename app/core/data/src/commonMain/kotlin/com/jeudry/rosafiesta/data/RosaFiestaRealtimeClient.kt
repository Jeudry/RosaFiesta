package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.RealtimeStreamEvent
import io.ktor.client.HttpClient
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

/**
 * Multiplatform Server-Sent Events (SSE) client connected to `/v1/stream` for real-time updates.
 */
public class RosaFiestaRealtimeClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val reconnectDelayMs: Long = 3_000L,
) {
    private val jsonCodec: Json = Json { ignoreUnknownKeys = true }

    /**
     * Subscribes to `/v1/stream` using [accessTokenProvider] and emits parsed domain stream events.
     */
    public fun observeStream(
        accessTokenProvider: () -> String?,
    ): Flow<RealtimeStreamEvent> = flow {
        var lastSequenceId = 0L
        while (currentCoroutineContext().isActive) {
            val token = accessTokenProvider()?.trim().orEmpty()
            if (token.isEmpty()) {
                delay(reconnectDelayMs)
                continue
            }
            try {
                lastSequenceId = consumeStreamOnce(
                    token = token,
                    lastSequenceId = lastSequenceId,
                    onEvent = { parsedEvent -> emit(parsedEvent) },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
            }
            delay(reconnectDelayMs)
        }
    }

    private suspend fun consumeStreamOnce(
        token: String,
        lastSequenceId: Long,
        onEvent: suspend (RealtimeStreamEvent) -> Unit,
    ): Long {
        var highestSeq = lastSequenceId
        val normalizedBase = baseUrl.trimEnd('/')
        httpClient.prepareGet("$normalizedBase/v1/stream") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "text/event-stream")
            if (highestSeq > 0L) {
                header("Last-Event-ID", highestSeq.toString())
            }
        }.execute { response ->
            if (!response.status.isSuccess()) {
                return@execute
            }
            val channel = response.bodyAsChannel()
            highestSeq = readSseFrames(
                channel = channel,
                initialSeq = highestSeq,
                onEvent = onEvent,
            )
        }
        return highestSeq
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
