@file:Suppress("DEPRECATION")

package com.adventist.adventist.infra.message_queue

import com.adventist.adventist.domain.events.AdventistEvent
import com.adventist.adventist.domain.events.chat.ChatEventConstants
import com.adventist.adventist.domain.events.user.UserEventConstants
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class RabbitMqConfig {
    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            findAndRegisterModules()

            // We need this because we cant serialize sealed classes data classes without it
            val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(AdventistEvent::class.java)
                .allowIfSubType("java.util.") // Allow java list
                .allowIfSubType("kotlin.collections.") // Allow kotlin list
                .build()

            activateDefaultTyping(
                polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
            )
        }

        return Jackson2JsonMessageConverter(objectMapper).apply {
            typePrecedence = Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: Jackson2JsonMessageConverter,
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        transactionManager: PlatformTransactionManager,
        messageConverter: Jackson2JsonMessageConverter
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            this.setTransactionManager(transactionManager)
            this.setConnectionFactory(connectionFactory)
            this.setChannelTransacted(true)
            this.setMessageConverter(messageConverter)
        }
    }

    @Bean
    fun userExchange() = TopicExchange(
        UserEventConstants.USER_EXCHANGE,
        true,
        false
    )

    @Bean
    fun chatExchange() = TopicExchange(
        ChatEventConstants.CHAT_EXCHANGE,
        true,
        false
    )


    @Bean
    fun chatUserEventsQueue() = Queue(
        MessageQueues.CHAT_USER_EVENTS,
        true
    )

    @Bean
    fun notificationUserEventsQueue() = Queue(
        MessageQueues.NOTIFICATION_USER_EVENTS,
        true
    )
  
  @Bean
  fun notificationChatEventsQueue() = Queue(
    MessageQueues.NOTIFICATION_CHAT_EVENTS,
    true
  )

    @Bean
    fun chatUserBinding(
        chatUserEventsQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(chatUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }

    @Bean
    fun notificationUserBinding(
        notificationUserEventsQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(notificationUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }
  
  // THE NAME OF THE PARAMETERS MATTERS A LOT FOR AUTOWIRING
  @Bean
  fun notificationChatBinding(
    notificationChatEventsQueue: Queue,
    chatExchange: TopicExchange
  ): Binding {
    return BindingBuilder
      .bind(notificationChatEventsQueue)
      .to(chatExchange)
      .with(ChatEventConstants.CHAT_NEW_MESSAGE)
  }
}