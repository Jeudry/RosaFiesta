package com.adventist.adventist.api.dto

import com.adventist.adventist.domain.types.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest(
    @field:Size(min = 1, message = "Chat must have at least two participants.")
    val otherUsersId: List<UserId>
){

}
