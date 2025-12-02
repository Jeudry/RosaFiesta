package com.adventist.adventist.infra.push_notification

import com.adventist.adventist.domain.model.DeviceToken
import com.adventist.adventist.domain.model.PushNotification
import com.adventist.adventist.domain.model.PushNotificationSendResult
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class FirebasePushNotificationService(
  @param:Value("\${firebase.credentials-path}") // Corregido el cierre de llave
  private val credentialsPath: String,
  private val resourceLoader: ResourceLoader,
) {
  private val logger = LoggerFactory.getLogger(FirebasePushNotificationService::class.java)
  
  @PostConstruct
  fun initialize(){
    try {
      val serviceAccount = resourceLoader.getResource(credentialsPath)
      if (!serviceAccount.exists()) {
        throw IllegalStateException("Firebase service account file not found at path: $credentialsPath")
      }
      val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount.inputStream))
        .build()
      FirebaseApp.initializeApp(options)
      logger.info("Firebase admin sdk initialized successfully.")
    } catch (e: Exception){
      logger.error("Error initializing Firebase admin sdk: ${e.message}", e)
      throw e
    }
  } 
  
  fun isValidToken(token: String): Boolean {
    val message = Message.builder()
      .setToken(token)
      .build()
    
    return try {
      FirebaseMessaging.getInstance().send(message, true)
      true
    } catch (ex: Exception){
      logger.warn("Failed to validate token: ${ex.message}", ex)
      false
    }
  }
  
  fun sendNotification(notification: PushNotification): PushNotificationSendResult {
    val messages = notification.recipients.map { recipient ->
      Message.builder()
        .setToken(recipient.token)
        .setNotification(
          Notification.builder()
            .setTitle(notification.title)
            .setBody(notification.message)
            .build()
        )
        .apply { 
          notification.data.forEach { (key, value) ->
            putData(key, value)
          }
          
          when(recipient.platform){
            DeviceToken.Platform.ANDROID -> {
              setAndroidConfig(
                AndroidConfig.builder()
                  .setPriority(AndroidConfig.Priority.HIGH)
                  .setCollapseKey(notification.chatId.toString())
                  .setRestrictedPackageName("com.adventist.adventist")
                  .build()
              )
            }
            DeviceToken.Platform.IOS -> {
              setApnsConfig(
                ApnsConfig.builder()
                  .setAps(
                    Aps.builder()
                      .setSound("default")
                      .setThreadId(notification.chatId.toString())
                      .build()
                  )
                  .build()
              )
            }
            DeviceToken.Platform.WEB -> {
              
            }
          }
        }
        .build()
    }
    
    return FirebaseMessaging.getInstance().sendEach(messages)
      .toSendResult(notification.recipients)
  }
  
  private fun BatchResponse.toSendResult(
    allDeviceTokens: List<DeviceToken>,
  ): PushNotificationSendResult {
    val succeeded = mutableListOf<DeviceToken>()
    val temporaryFailures = mutableListOf<DeviceToken>()
    val permanentFailures = mutableListOf<DeviceToken>()
    
    responses.forEachIndexed { index, sendResponse ->
      val deviceToken = allDeviceTokens[index]
      if(sendResponse.isSuccessful){
        succeeded.add(allDeviceTokens[index])
      } else {
        val errorCode = sendResponse.exception?.messagingErrorCode
        
        logger.warn("Failed to send notification to token ${allDeviceTokens[index].token}: ${sendResponse.exception?.message}")
        
        when(errorCode){
          MessagingErrorCode.UNREGISTERED,
          MessagingErrorCode.INVALID_ARGUMENT,
          MessagingErrorCode.SENDER_ID_MISMATCH,
          MessagingErrorCode.THIRD_PARTY_AUTH_ERROR -> {
            permanentFailures.add(deviceToken)
          }
          MessagingErrorCode.INTERNAL,
          MessagingErrorCode.UNAVAILABLE,
          MessagingErrorCode.QUOTA_EXCEEDED -> TODO()
          null -> {
            temporaryFailures.add(allDeviceTokens[index])
          }
        }
      }
    }
    
    logger.debug("Push notification send result: ${succeeded.size} succeeded, ${temporaryFailures.size} temporary failures, ${permanentFailures.size} permanent failures")
    
    return PushNotificationSendResult(
      succeded = succeeded,
      temporaryFailures = temporaryFailures,
      permanentFailures = permanentFailures
    )
  }
}