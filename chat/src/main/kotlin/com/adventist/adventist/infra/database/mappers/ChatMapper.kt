package com.adventist.adventist.infra.database.mappers

import com.adventist.adventist.domain.models.Chat
import com.adventist.adventist.domain.models.ChatMessage
import com.adventist.adventist.infra.database.entities.ChatEntity

fun ChatEntity.toModel(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = id!!,
        participants = participants.map {
            it.toModel()
        }.toSet(),
        creator = creator!!.toModel(),
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt,
        lastMessage = lastMessage
    )
}