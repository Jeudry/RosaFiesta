package com.example.core.domain.validators

interface PatternValidator {
  fun matches(value: String): Boolean
}