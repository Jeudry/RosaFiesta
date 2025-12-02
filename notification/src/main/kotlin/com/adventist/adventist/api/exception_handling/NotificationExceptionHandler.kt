package com.adventist.adventist.api.exception_handling

import com.adventist.adventist.domain.exceptions.InvalidDeviceTokenEx
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NotificationExceptionHandler {
  @ExceptionHandler(InvalidDeviceTokenEx::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun onInvalidDeviceTokenEx(ex: InvalidDeviceTokenEx) = mapOf(
    "message" to ex.message.orEmpty(),
    "code" to "INVALID_DEVICE_TOKEN"
  )
}