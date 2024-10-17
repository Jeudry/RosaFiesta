package com.example.auth.data

import android.util.Patterns
import com.example.core.domain.validators.PatternValidator

object EmailPatternValidator : PatternValidator {


  override fun matches(value: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(value).matches()
  }
}