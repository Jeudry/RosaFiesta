package com.adventist.adventist.infra.service

import com.adventist.adventist.domain.types.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value("\${adventist.email.from}")
    private val emailFrom: String,
    @param:Value("\${adventist.email.url}")
    private val baseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendVerificationEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String
    ){
        logger.info("Sending verification email for user: $userId")

        val verificationUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/verify")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            "emails/account-verification",
            mapOf(
                "username" to username,
                "verificationUrl" to verificationUrl
            )
        )

        sendHtmlEmail(email, "Verify your Adventist account", htmlContent)
    }

    fun sendPasswordResetEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String,
        expiresInMinutes: Duration
    ){
        logger.info("Sending password reset email for user: $userId")

        val resetPasswordUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/reset-password")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            "emails/account-verification",
            mapOf(
                "username" to username,
                "resetPasswordUrl" to resetPasswordUrl,
                "expiresInMinutes" to expiresInMinutes.toMinutes()
            )
        )

        sendHtmlEmail(email, "Reset your Adventist password", htmlContent)
    }


    private fun sendHtmlEmail(
        to: String,
        subject: String,
        htmlContent: String
    ){
        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, "UTF-8").apply {
            setTo(to)
            setSubject(subject)
            setText(htmlContent, true)
            setFrom(emailFrom)
        }

         try {
             javaMailSender.send(message)
         } catch (e: Exception) {
             logger.error("Failed to send email to $to", e)
         }
    }
}