@file:Suppress("DEPRECATION")

package com.adventist.adventist.infra.caching

import com.adventist.adventist.domain.events.AdventistEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig(){
    @Bean
    fun cacheManager(
        connectionFactory: LettuceConnectionFactory
    ): RedisCacheManager {
        val objectMapper = ObjectMapper().apply {
            // allows to parse java time instances to json
            registerModule(JavaTimeModule())

            registerModule(KotlinModule.Builder().build())
            findAndRegisterModules()

            // We need this because we cant serialize sealed classes data classes without it
            val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util.") // Allow java list
                .allowIfSubType("kotlin.collections.") // Allow kotlin list
                .allowIfSubType("com.adventist.adventist.")
                .build()

            activateDefaultTyping(
                polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
            )
        }

        val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1L))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )
            .disableCachingNullValues()

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfig)
            .withCacheConfiguration(
                "messages",
                    cacheConfig.entryTtl(Duration.ofMinutes(30))
            )
            .transactionAware()
             .build()
    }
}
