package com.adventist.adventist.api.dto

import com.adventist.adventist.domain.types.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToChatDto(
    @field:Size(min=1, message = "Chat must have at least two participants.")
    val userIds: List<UserId>
)
