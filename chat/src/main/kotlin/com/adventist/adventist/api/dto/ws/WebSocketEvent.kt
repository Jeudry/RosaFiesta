package com.adventist.adventist.api.dto.ws

enum class IncomingWebSocketMessageType {
    NEW_MESSAGE
}

enum class OutgoingWebSocketMessageType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED,
    ERROR
}

data class IncomingWebsocketMessage(
    val type: IncomingWebSocketMessageType,
    val payload: String
)

data class OutgoingWebsocketMessage(
    val type: OutgoingWebSocketMessageType,
    val payload: String
)