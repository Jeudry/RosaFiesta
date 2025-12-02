package com.adventist.adventist.domain.exceptions

import com.adventist.adventist.domain.types.ChatMessageId

class ChatMessageNotFoundEx(
    private val id: ChatMessageId
): RuntimeException("Chat message with id $id not found")