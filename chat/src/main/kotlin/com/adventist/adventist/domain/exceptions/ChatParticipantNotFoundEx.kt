package com.adventist.adventist.domain.exceptions

import com.adventist.adventist.domain.types.UserId

class ChatParticipantNotFoundEx(
    private val id: UserId
): RuntimeException("Chat participant with id $id not found")