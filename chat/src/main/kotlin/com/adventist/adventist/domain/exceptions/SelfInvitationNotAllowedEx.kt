package com.adventist.adventist.domain.exceptions

import com.adventist.adventist.domain.types.UserId

class SelfInvitationNotAllowedEx(userId: UserId): RuntimeException(
    "User cannot invite themselves to a chat: $userId"
)
