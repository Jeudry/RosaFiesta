package com.example.auth.presentation.register

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.domain.PasswordValidationState
import com.example.auth.domain.UserDataValidator
import com.example.auth.presentation.R
import com.example.core.presentation.designsystem.CheckIcon
import com.example.core.presentation.designsystem.CrossIcon
import com.example.core.presentation.designsystem.EmailIcon
import com.example.core.presentation.designsystem.RFColors
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.bodyFontFamily
import com.example.core.presentation.designsystem.components.RFPasswordTextField
import com.example.core.presentation.designsystem.components.RFPrimaryLargeButton
import com.example.core.presentation.designsystem.components.RFScreenGradientBackground
import com.example.core.presentation.designsystem.components.RFParticleBackground
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
  state: RegisterState,
  onAction: (RegisterAction) -> Unit
) {
  RFScreenGradientBackground {
    RFParticleBackground {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Card(
          modifier = Modifier
            .width(320.dp)
            .wrapContentHeight(),
          shape = CardDefaults.shape,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
          Column(
            modifier = Modifier.padding(35.dp),
            horizontalAlignment = Alignment.Start
          ) {
            Text(
              text = stringResource(id = R.string.create_account),
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground,
              fontFamily = bodyFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = stringResource(id = R.string.runique_welcome_text),
              fontSize = 13.sp,
              color = RFColors.Subtitle,
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
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
            Spacer(modifier = Modifier.height(20.dp))
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
            Spacer(modifier = Modifier.height(20.dp))
            PasswordRequirement(
              text = stringResource(
                id = R.string.at_least_x_characters,
                UserDataValidator.MIN_PASSWORD_LENGTH
              ),
              isValid = state.passwordValidationState.hasMinLength
            )
            Spacer(modifier = Modifier.height(12.dp))
            PasswordRequirement(
              text = stringResource(id = R.string.at_least_one_number),
              isValid = state.passwordValidationState.hasNumber
            )
            Spacer(modifier = Modifier.height(12.dp))
            PasswordRequirement(
              text = stringResource(id = R.string.contains_lowercase_char),
              isValid = state.passwordValidationState.hasLowerCaseCharacter
            )
            Spacer(modifier = Modifier.height(12.dp))
            PasswordRequirement(
              text = stringResource(id = R.string.contains_uppercase_char),
              isValid = state.passwordValidationState.hasUpperCaseCharacter
            )
            Spacer(modifier = Modifier.height(24.dp))
            RFPrimaryLargeButton(
              text = stringResource(id = R.string.register),
              loading = state.isRegistering,
              enabled = state.canRegister,
              modifier = Modifier.fillMaxWidth()
            ) { onAction(RegisterAction.OnRegisterClick) }
            Spacer(modifier = Modifier.height(25.dp))
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = stringResource(id = R.string.already_have_an_account),
                color = RFColors.Subtitle,
                fontSize = 13.sp,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = stringResource(id = R.string.login),
                color = RFColors.Link,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onAction(RegisterAction.OnLoginClick) }
              )
            }
          }
        }
      }
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

@OptIn(ExperimentalFoundationApi::class)
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