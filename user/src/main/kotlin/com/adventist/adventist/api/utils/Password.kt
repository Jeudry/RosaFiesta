package com.adventist.adventist.api.utils

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@Repeatable
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
    message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
)
annotation class Password(
    val message: String = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Payload>> = [],


)
