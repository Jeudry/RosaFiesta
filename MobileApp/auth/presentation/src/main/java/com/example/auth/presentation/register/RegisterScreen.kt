package com.example.auth.presentation.register

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.domain.PasswordValidationState
import com.example.auth.domain.UserDataValidator
import com.example.auth.presentation.R
import com.example.core.presentation.designsystem.CheckIcon
import com.example.core.presentation.designsystem.CrossIcon
import com.example.core.presentation.designsystem.EmailIcon
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.bodyFontFamily
import com.example.core.presentation.designsystem.components.GradientBackground
import com.example.core.presentation.designsystem.components.RFActionButton
import com.example.core.presentation.designsystem.components.RFPasswordTextField
import com.example.core.presentation.designsystem.components.RFTextField
import com.example.core.presentation.ui.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreenRoot(
  onSignInClick: () -> Unit,
  onSuccessfulRegistration: () -> Unit,
  viewModel: RegisterViewModel = koinViewModel()
) {
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current
  ObserveAsEvents(viewModel.events) { event ->
    when (event) {
      is RegisterEvent.Error -> {
        keyboardController?.hide()
        Toast.makeText(
          context,
          event.error.asString(context = context),
          Toast.LENGTH_LONG
        ).show()
      }

      RegisterEvent.RegistrationSuccess -> {
        keyboardController?.hide()
        Toast.makeText(
          context,
          R.string.registration_successful,
          Toast.LENGTH_LONG
        ).show()
        onSuccessfulRegistration()
      }
    }
  }

  RegisterScreen(
    state = viewModel.state,
    onAction = { action ->
      when (action) {
        is RegisterAction.OnLoginClick -> onSignInClick()
        else -> viewModel.onAction(action)
      }
    }
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
  state: RegisterState,
  onAction: (RegisterAction) -> Unit
) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .padding(vertical = 32.dp)
        .padding(top = 16.dp)
    ) {
      Text(
        text = stringResource(id = R.string.create_account),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
      val annotatedString = buildAnnotatedString {
        withStyle(
          style = SpanStyle(
            fontFamily = bodyFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        ) {
          append(stringResource(id = R.string.already_have_an_account) + " ")
          pushStringAnnotation(
            tag = "clickable_text",
            annotation = stringResource(id = R.string.login)
          )
          withStyle(
            style = SpanStyle(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
              fontFamily = bodyFontFamily
            )
          ) {
            append(stringResource(id = R.string.login))
          }
        }
      }
      ClickableText(
        text = annotatedString,
        onClick = { offset ->
          annotatedString.getStringAnnotations(
            tag = "clickable_text",
            start = offset,
            end = offset
          ).firstOrNull()?.let {
            onAction(RegisterAction.OnLoginClick)
          }
        }
      )
      Spacer(modifier = Modifier.height(48.dp))
      RFTextField(
        state = state.email,
        startIcon = EmailIcon,
        endIcon = if (state.isEmailValid) CheckIcon else null,
        hint = stringResource(id = R.string.example_email),
        title = stringResource(id = R.string.email),
        modifier = Modifier.fillMaxWidth(),
        additionalInfo = stringResource(id = R.string.must_be_a_valid_email),
        keyboardType = KeyboardType.Email
      )
      Spacer(modifier = Modifier.height(16.dp))
      RFPasswordTextField(
        state = state.password,
        hint = stringResource(id = R.string.password),
        title = stringResource(id = R.string.password),
        modifier = Modifier.fillMaxWidth(),
        isVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = {
          onAction(RegisterAction.OnTogglePasswordVisibilityClick)
        }
      )
      Spacer(modifier = Modifier.height(16.dp))
      PasswordRequirement(
        text = stringResource(
          id = R.string.at_least_x_characters,
          UserDataValidator.MIN_PASSWORD_LENGTH
        ),
        isValid = state.passwordValidationState.hasMinLength
      )
      Spacer(modifier = Modifier.height(16.dp))
      PasswordRequirement(
        text = stringResource(
          id = R.string.at_least_one_number
        ),
        isValid = state.passwordValidationState.hasNumber
      )
      Spacer(modifier = Modifier.height(16.dp))
      PasswordRequirement(
        text = stringResource(
          id = R.string.contains_lowercase_char
        ),
        isValid = state.passwordValidationState.hasLowerCaseCharacter
      )
      Spacer(modifier = Modifier.height(16.dp))
      PasswordRequirement(
        text = stringResource(id = R.string.contains_uppercase_char),
        isValid = state.passwordValidationState.hasUpperCaseCharacter
      )
      Spacer(modifier = Modifier.height(32.dp))
      RFActionButton (
        text = stringResource(id = R.string.register),
        isLoading = state.isRegistering,
        enabled = state.canRegister,
        modifier = Modifier.fillMaxWidth()
      ) {
        onAction(RegisterAction.OnRegisterClick)
      }
    }
}

@Composable
fun PasswordRequirement(
  text: String,
  isValid: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (isValid) CheckIcon else CrossIcon, contentDescription = null,
      tint = if (isValid) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = text,
      color = MaterialTheme.colorScheme.onBackground,
      fontSize = 14.sp
    )
  }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun RegisterScreenPreview() {
  RFTheme {
    RegisterScreen(
      state = RegisterState(
        passwordValidationState = PasswordValidationState(
          hasMinLength = true,
          hasNumber = true,
          hasLowerCaseCharacter = true,
          hasUpperCaseCharacter = true
        )
      ),
      onAction = { }
    )
  }
}