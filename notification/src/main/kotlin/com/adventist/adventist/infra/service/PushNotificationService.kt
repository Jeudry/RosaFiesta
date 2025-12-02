package com.adventist.adventist.infra.service

import com.adventist.adventist.domain.exceptions.InvalidDeviceTokenEx
import com.adventist.adventist.domain.model.DeviceToken
import com.adventist.adventist.domain.model.PushNotification
import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.UserId
import com.adventist.adventist.infra.database.DeviceTokenEntity
import com.adventist.adventist.infra.database.DeviceTokenRepository
import com.adventist.adventist.infra.mappers.toEntity
import com.adventist.adventist.infra.mappers.toModel
import com.adventist.adventist.infra.push_notification.FirebasePushNotificationService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

@Service
class PushNotificationService(
  private val deviceTokenRepository: DeviceTokenRepository,
  private val firebasePushNotificationService: FirebasePushNotificationService
) {
  companion object {
    private val RETRAY_DELAY_SECONDS = listOf(
      30L, 60L, 120L, 300L, 600L
    )
    const val MAX_RETRY_AGE_MINUTES = 30L
  }
  
  private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()
  
  private val logger = LoggerFactory.getLogger(javaClass)
  
  fun registerDevice(
    userId: UserId,
    token: String,
    platform: DeviceToken.Platform
  ): DeviceToken {
    val existing = deviceTokenRepository.findByToken(token)
    
    val trimmedToken = token.trim()
    if(existing == null && !firebasePushNotificationService.isValidToken(trimmedToken)) {
      throw InvalidDeviceTokenEx()
    }
    
    val entity = if(existing != null){
      deviceTokenRepository.save(
        existing.apply { 
          this.userId = userId
        }
      )
    } else {
      deviceTokenRepository.save(
        DeviceTokenEntity(
          userId = userId,
          token = trimmedToken,
          platform = platform.toEntity()
        )
      )
    }
    
    return entity.toModel()
  }
  
  @Transactional
  fun unregisterDevice(token: String) {
    deviceTokenRepository.deleteByToken(token.trim())
  }
  
  fun sendNewMessageNotification(
    recipientUserIds: List<UserId>,
    senderUserId: UserId,
    senderUsername: String,
    message: String,
    chatId: ChatId
  ){
    val deviceTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)
    
    if(deviceTokens.isEmpty()){
      logger.info("No device tokens found for $recipientUserIds")
      return
    }
    
    val recipients = deviceTokens.filter { 
      it.userId != senderUserId
    }.map { deviceToken -> deviceToken.toModel() }
    
    val notification = PushNotification(
      title = "New message from $senderUsername",
      recipients = recipients,
      message = message,
      chatId = chatId,
      data = mapOf(
        "chatId" to chatId.toString(),
        "type" to "new_message",
      )
    )
    
    sendWithRetry(notification)
  }
  
  fun sendWithRetry(
    notification: PushNotification,
    attempt: Int = 0
  ){
    val result = firebasePushNotificationService.sendNotification(notification)
    
    result.permanentFailures.forEach { 
      deviceTokenRepository.deleteByToken(it.token)
    }
    
    if(result.temporaryFailures.isNotEmpty() && attempt < RETRAY_DELAY_SECONDS.size) {
      val retryNotification = notification.copy(
        recipients = result.temporaryFailures
      )
      
      scheduleRetry(retryNotification, attempt + 1)
    }
    
    if(result.succeded.isNotEmpty()){
      logger.info("Succesfully sent notification to ${result.succeded.size} devices")
    }
  }
  
  private fun scheduleRetry(retryNotification:PushNotification, attempt: Int) {
    val delay = RETRAY_DELAY_SECONDS.getOrElse(attempt - 1){
      RETRAY_DELAY_SECONDS.last()
    }
    val executeAt = Instant.now().plusSeconds(delay)
    val executeAtMillis = executeAt.toEpochMilli()
    
    val retryData = RetryData(
      notification = retryNotification,
      attempt = attempt,
      createdAt = Instant.now()
    )
    
    retryQueue.compute(executeAtMillis) { _, retries ->
      (retries ?: mutableListOf()).apply {
        add(retryData)
      }
    }
    
    logger.info("Scheduled retry #$attempt for ${retryNotification.id} at $executeAt")
  }
  
  @Scheduled(fixedDelay = 15_000L)
  fun processRetries(){
    val now = Instant.now()
    val nowInMillis = now.toEpochMilli()
    val toProcess = retryQueue.headMap(nowInMillis, true)
    
    if(toProcess.isEmpty()){
      return
    }
    
    val entries = toProcess.entries.toList()
    
    entries.forEach { (timeMillis, retries) ->
      retryQueue.remove(timeMillis)
      
      retries.forEach { retry ->
        try {
          val age = Duration.between(retry.createdAt, now)
          if (age.toMinutes() > MAX_RETRY_AGE_MINUTES) {
            logger.warn("Dropping retry for notification ${retry.notification.id} due to age (${age.toMinutes()} minutes)")
            return@forEach
          }
          
          sendWithRetry(
            notification = retry.notification,
            attempt = retry.attempt
          )
        } catch (ex: Exception){
          logger.warn("Error processing retry for notification ${retry.notification.id}", ex)
        }
      }
    }
  }
  
  private data class RetryData(
    val notification: PushNotification,
    val attempt: Int,
    val createdAt: Instant
  )
}