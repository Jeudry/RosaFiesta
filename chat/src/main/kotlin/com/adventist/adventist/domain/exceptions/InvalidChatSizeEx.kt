package com.adventist.adventist.domain.exceptions

class InvalidChatSizeEx: RuntimeException(
    "There must be at least two participants in a chat"
)