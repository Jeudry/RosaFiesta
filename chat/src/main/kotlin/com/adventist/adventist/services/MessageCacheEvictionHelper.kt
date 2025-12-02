package com.adventist.adventist.services

import com.adventist.adventist.domain.types.ChatId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class MessageCacheEvictionHelper {
  
  @CacheEvict(
    value = ["messages"],
    key = "#chatId",
  )
  fun evictMessagesCache(chatId: ChatId){
    // NO-OP: Let spring handle cache eviction
    
  }
  
}