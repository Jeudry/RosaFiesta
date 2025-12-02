package com.adventist.adventist.api.websocket

import com.adventist.adventist.api.dto.ws.*
import com.adventist.adventist.api.mappers.toDto
import com.adventist.adventist.domain.events.*
import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.UserId
import com.adventist.adventist.services.ChatMessageService
import com.adventist.adventist.services.ChatService
import com.adventist.adventist.services.JwtService
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
  private val chatMessageService: ChatMessageService,
  private val objectMapper: ObjectMapper,
  private val chatService: ChatService,
  private val jwtService: JwtService,
) : TextWebSocketHandler() {
  
  companion object {
    private const val PING_INTERVAL_MS = 30_000L
    private const val PONG_TIMEOUT_MS = 60_000L
  }
  
  private val logger = LoggerFactory.getLogger(javaClass)
  
  private val connectionLock = ReentrantReadWriteLock()
  private val sessions = ConcurrentHashMap<String, UserSession>()
  private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
  private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
  private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()
  private val mapper = objectMapper.registerModule(JavaTimeModule())
  
  private fun broadcastToChat(
    chatId: ChatId,
    message: OutgoingWebsocketMessage
  ) {
    val chatSessions = connectionLock.read {
      chatToSessions[chatId]?.toSet() ?: return
    }
    
    chatSessions.forEach { session ->
      val userSession = connectionLock.read {
        sessions[session]
      } ?: return@forEach
      
      sendToUser(
        userId = userSession.userId,
        message = message
      )
    }
  }
  
  override fun afterConnectionEstablished(session: WebSocketSession) {
    val authHeader = session
      .handshakeHeaders
      .getFirst(HttpHeaders.AUTHORIZATION)
      ?: run {
        logger.warn("Session ${session.id}  closed due to missing Authorization header")
        session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
        return
      }
    
    val userId = jwtService.getUserIdFromToken(authHeader)
    
    val userSession = UserSession(
      userId = userId,
      session = session,
    )
    
    // synchronize all the connection related maps
    // What it does? It adds the session to sessions map,
    // adds the session id to userToSessions map,
    // adds the chat ids to userChatIds map if not present,
    // and adds the session id to chatToSessions map for each chat id.
    connectionLock.write {
      sessions[session.id] = userSession
      
      // allows to read and write userToSessions and chatToSessions maps atomically
      userToSessions.compute(userId) { _, existingSessions ->
        (existingSessions ?: mutableSetOf()).apply { add(session.id) }
      }
      
      val chatIds = userChatIds.computeIfAbsent(userId) {
        val chatIds = chatService.findChatsByUser(userId).map { it.id }
        ConcurrentHashMap.newKeySet<ChatId>().apply {
          addAll(chatIds)
        }
      }
      
      chatIds.forEach { chatId ->
        chatToSessions.compute(chatId) { _, sessions ->
          (sessions ?: mutableSetOf()).apply {
            add(session.id)
          }
        }
      }
      
      logger.info("Websocket connection established for user $userId")
    }
  }
  
  private fun handleSendMessage(
    dto: SendMessageDto,
    senderId: UserId
  ) {
    val userChatIds = connectionLock.read {
      this@ChatWebSocketHandler.userChatIds[senderId]?.toSet() ?: return
    }
    
    if (dto.chatId !in userChatIds) {
      return
    }
    
    val savedMessage = chatMessageService.sendMessage(
      chatId = dto.chatId!!,
      senderId = senderId,
      content = dto.content,
      messageId = dto.messageId
    )
    
    broadcastToChat(
      chatId = dto.chatId,
      message = OutgoingWebsocketMessage(
        type = OutgoingWebSocketMessageType.NEW_MESSAGE,
        payload = objectMapper.writeValueAsString(savedMessage.toDto()),
      )
    )
  }
  
  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    logger.debug("Received message: ${message.payload} from session: ${session.id}")
    
    val userSession = connectionLock.read {
      sessions[session.id] ?: return
    }
    
    try {
      val webSocketMessage = objectMapper.readValue(
        message.payload,
        IncomingWebsocketMessage::class.java
      )
      when (webSocketMessage.type) {
        IncomingWebSocketMessageType.NEW_MESSAGE -> {
          val dto = objectMapper.readValue(
            webSocketMessage.payload,
            SendMessageDto::class.java
          )
          handleSendMessage(dto, userSession.userId)
        }
      }
    } catch (ex: JsonMappingException) {
      logger.warn("Could not parse message ${message.payload}", ex)
      sendError(
        session,
        WsErrorDto(
          code = "INVALID_JSON",
          message = "Incoming JSON or UUID is invalid"
        )
      )
    }
  }
  
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun onDeleteMessage(
    event: MessageDeletedEvent,
  ){
    broadcastToChat(
      chatId = event.chatId,
      message = OutgoingWebsocketMessage(
        type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
        payload = objectMapper.writeValueAsString(
          DeleteMessageDto(
            chatId = event.chatId,
            messageId = event.messageId,
          )
        )
      )
    )
  }
  
  private fun updateChatForUsers(
    chatId: ChatId,
    userIds: List<UserId>
  ){
    connectionLock.write {
      userIds.forEach { userId ->
        userChatIds.compute(userId) { _, existingChatIds ->
          (existingChatIds ?: mutableSetOf()).apply {
            add(chatId)
          }
        }
        userToSessions[userId]?.forEach { session ->
          chatToSessions.compute(chatId) { _, existingSessions ->
            (existingSessions ?: mutableSetOf()).apply {
              add(session)
            }
          }
        }
      }
    }
  }
  
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun onChatCreated(event: ChatCreatedEvent){
    updateChatForUsers(
      chatId = event.chatId,
      userIds = event.participantIds
    )
  }
  
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun onJoinChat(event: ChatParticipantsJoinedEvent){
    updateChatForUsers(
      chatId = event.chatId,
      userIds = event.usersId.toList()
    )
    
    broadcastToChat(
      chatId = event.chatId,
      message = OutgoingWebsocketMessage(
        type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
        payload = objectMapper.writeValueAsString(
          ChatParticipantsChangedDto(
            chatId = event.chatId,
          )
        )
      )
    )
  }
  
  @Scheduled(fixedDelay = PING_INTERVAL_MS)
  fun pingClients(){
    val currentTime = System.currentTimeMillis()
    val sessionToClose = mutableListOf<String>()
    
    val sessionsSnapshot = connectionLock.read {
      sessions.toMap()
    }
    
    sessionsSnapshot.forEach { (sessionId, userSession) ->
      try {
        if(!userSession.session.isOpen){
          val lastPong = userSession.lastPongTimestamp
          if(currentTime - lastPong > PONG_TIMEOUT_MS){
            logger.warn("Session $sessionId is closed and timed out. Closing it.")
            sessionToClose.add(sessionId)
            return@forEach
          }
          
          userSession.session.sendMessage(PingMessage())
          logger.warn("Sent ping to closed session $sessionId")
        }
      } catch (e: Exception){
        logger.error("Error while pinging session $sessionId", e)
        sessionToClose.add(sessionId)
      }
    }
    
    sessionToClose.forEach { sessionId ->
      connectionLock.read {
        sessions[sessionId]?.session?.let { session ->
          try {
            session.close(CloseStatus.GOING_AWAY.withReason(
              "Ping timeout"
            ))
          } catch (ex: Exception){
            logger
          }
        }
      }
    }
  }
  
  override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
    connectionLock.write {
      sessions.compute(session.id){ _, userSession ->
        userSession?.copy(
          lastPongTimestamp = System.currentTimeMillis()
        )
      }
    }
    logger.info("Received ong message ${message.payload} from session ${session.id}")
  }
  
  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    connectionLock.write {
      sessions.remove(session.id)?.let { userSession ->
        val userId = userSession.userId
        
        userToSessions.compute(userId){ _, sessions ->
          sessions?.apply { 
            remove(session.id)
          }?.takeIf { 
            it.isNotEmpty()
          }
        }
        
        userChatIds[userId]?.forEach { chatId ->
          chatToSessions.compute(chatId){ _, sessions ->
            sessions?.apply {
              remove(session.id)
            }?.takeIf {
              it.isNotEmpty()
            }
          }
        }
        
        logger.info("Websocket connection closed for user $userId")
      }
    }
  }
  
  override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
    logger.error("Transport error in session ${session.id}", exception)
    session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"))
    
  }
  
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun onLeftChat(event: ChatParticipantLeftEvent){
    connectionLock.write {
      userChatIds.compute(event.userId){ _, chatIds ->
        chatIds?.apply { remove(event.chatId) }
          ?.takeIf { it.isNotEmpty() }
      }
      userToSessions[event.userId]?.forEach { session ->
        chatToSessions.compute(event.chatId) { _, existingSessions ->
          existingSessions?.apply {
            remove(session)
            takeIf { it.isNotEmpty() }
          }
        }
      }
    }
    broadcastToChat(
      chatId = event.chatId,
      message = OutgoingWebsocketMessage(
        type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
        payload = objectMapper.writeValueAsString(
          ChatParticipantsChangedDto(
            chatId = event.chatId,
          )
        )
      )
    )
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun onProfilePictureUpdated(event: ProfilePictureUpdatedEv){
    val userChats = connectionLock.read { 
      userChatIds[event.userId]?.toList() ?: emptyList()
    }
    val dto = ProfilePictureUpdateDto(
      userId = event.userId,
      newUrl = event.newUrl
    )
    val sessionIds = mutableSetOf<String>()
    userChats.forEach { chatId ->
      connectionLock.read {
        chatToSessions[chatId]?.let {  sessions ->
          sessionIds.addAll(sessions)
        }
      }
    }
    val webSocketMessage = OutgoingWebsocketMessage(
      type = OutgoingWebSocketMessageType.PROFILE_PICTURE_UPDATED,
      payload = mapper.writeValueAsString(dto)
    )
    val messageJson = mapper.writeValueAsString(webSocketMessage)
    sessionIds.forEach { sessionId ->
      val userSession = connectionLock.read {
        sessions[sessionId] ?: return@forEach
      }
      try {
        if(userSession.session.isOpen){
          userSession.session.sendMessage(TextMessage(messageJson))
        }
      } catch (e: Exception){
        logger.error("Could not send profile picture update to session $sessionId", e)
      }
    }
  }
  
  private fun sendError(
    session: WebSocketSession,
    error: WsErrorDto
  ){
    val webSocketMessage = objectMapper.writeValueAsString(
      OutgoingWebsocketMessage(
        type = OutgoingWebSocketMessageType.ERROR,
        payload = objectMapper.writeValueAsString(error)
      )
    )
    
    try {
      session.sendMessage(TextMessage(webSocketMessage))
    } catch (ex: Exception) {
      logger.error("Error while sending error message to session ${session.id}", ex)
    }
  }
  
  private fun sendToUser(userId: UserId, message: OutgoingWebsocketMessage) {
    val userSessions = connectionLock.read {
      userToSessions[userId] ?: emptySet()
    }
    
    userSessions.forEach { sessionId ->
      val userSession = connectionLock.read {
        sessions[sessionId] ?: return@forEach
      }
      if (userSession.session.isOpen) {
        try {
          val messageJson = objectMapper.writeValueAsString(message)
          userSession.session.sendMessage(TextMessage(messageJson))
          logger.debug("Message sent to user $userId: $messageJson")
        } catch (ex: Exception) {
          logger.error("Error while sending message to user $userId", ex)
        }
      }
    }
  }
  
  private data class UserSession(
    val userId: UserId,
    val session: WebSocketSession,
    val lastPongTimestamp: Long = System.currentTimeMillis()
  )
}