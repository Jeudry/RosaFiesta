@file:OptIn(ExperimentalFoundationApi::class)

package com.example.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.presentation.R
import com.example.auth.presentation.ui.AuthTitleBlock
import com.example.core.presentation.designsystem.EmailIcon
import com.example.core.presentation.designsystem.RFColors
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.components.RFLabeledDivider
import com.example.core.presentation.designsystem.components.RFParticleBackground
import com.example.core.presentation.designsystem.components.RFPasswordTextField
import com.example.core.presentation.designsystem.components.RFPrimaryLargeButton
import com.example.core.presentation.designsystem.components.RFScreenGradientBackground
import com.example.core.presentation.designsystem.components.RFSocialButton
import com.example.core.presentation.designsystem.components.RFTextField
import com.example.core.presentation.ui.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Suppress("unused")
const val LOGIN_ROUTE = "login"

@Composable
fun LoginScreenRoot(
  onLoginSuccess: () -> Unit,
  onSignUpClick: () -> Unit,
  viewModel: LoginViewModel = koinViewModel()
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

  ObserveAsEvents(viewModel.events) { event ->
    when (event) {
      is LoginEvent.Error -> {
        keyboardController?.hide()
        Toast.makeText(
          context,
          event.error.asString(context),
          Toast.LENGTH_LONG
        ).show()
      }
      LoginEvent.LoginSuccess -> {
        keyboardController?.hide()
        Toast.makeText(
          context,
          R.string.youre_logged_in,
          Toast.LENGTH_LONG
        ).show()
        onLoginSuccess()
      }
    }
  }
  LoginScreen(
    state = viewModel.state,
    onAction = { action ->
      when (action) {
        is LoginAction.OnRegisterClick -> onSignUpClick()
        else -> Unit
      }
      viewModel.onAction(action)
    }
  )
}

@Composable
fun LoginScreen(state: LoginState, onAction: (LoginAction) -> Unit) {
  RFScreenGradientBackground {
    RFParticleBackground {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        LoginForm(
          state = state,
          onAction = onAction
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
  state: LoginState,
  onAction: (LoginAction) -> Unit
) {
  Card(
    modifier = Modifier
      .width(320.dp)
      .wrapContentHeight(),
    shape = RoundedCornerShape(25.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
  ) {
    Column(
      modifier = Modifier.padding(35.dp),
      horizontalAlignment = Alignment.Start
    ) {
      AuthTitleBlock(
        title = stringResource(id = R.string.sign_in_title_placeholder, "Sign In").removePrefix("").ifEmpty { "Sign In" },
        subtitle = stringResource(id = R.string.sign_in_subtitle_placeholder, "Please sign in to your account first").removePrefix("").ifEmpty { "Please sign in to your account first" }
      )

      RFTextField(
        state = state.email,
        startIcon = EmailIcon,
        endIcon = null,
        keyboardType = KeyboardType.Email,
        hint = stringResource(id = R.string.example_email),
        title = stringResource(id = R.string.email),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      RFPasswordTextField(
        state = state.password,
        onTogglePasswordVisibility = {
          onAction(
            LoginAction.OnTogglePasswordVisibility
          )
        },
        isVisible = state.isPasswordVisible,
        modifier = Modifier.fillMaxWidth(),
        hint = stringResource(id = R.string.password),
        title = stringResource(id = R.string.password),
      )

      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Checkbox(
              checked = state.rememberMe,
              onCheckedChange = { onAction(LoginAction.OnToggleRememberMe) },
              modifier = Modifier.size(20.dp)
            )
          }
          Text(
            text = stringResource(id = R.string.remember_me),
            fontSize = 12.sp,
            color = RFColors.Subtitle,
            modifier = Modifier.padding(start = 8.dp)
          )
        }
        Text(
          text = stringResource(id = R.string.forgot_password_short),
          color = RFColors.Link,
          fontSize = 12.sp,
          modifier = Modifier.clickable { /* TODO: forgot password */ },
          textAlign = TextAlign.End
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      RFPrimaryLargeButton(
        text = stringResource(id = R.string.sign_in_button_placeholder, "Sign In").ifEmpty { "Sign In" },
        loading = state.isLoggingIn,
        enabled = state.canLogin && !state.isLoggingIn,
        modifier = Modifier.fillMaxWidth()
      ) { onAction(LoginAction.OnLoginClick) }


      Spacer(modifier = Modifier.height(25.dp))

      RFLabeledDivider(
        text = stringResource(id = R.string.or_sign_in_using_placeholder, "Or sign in using:"),
        modifier = Modifier.fillMaxWidth(),
        textColor = RFColors.Subtitle
      )

      Spacer(modifier = Modifier.height(25.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        RFSocialButton(
          backgroundColor = RFColors.SocialFacebook,
          icon = Icons.Default.Person,
          onClick = { /* Facebook */ }
        )
        Spacer(modifier = Modifier.width(15.dp))
        RFSocialButton(
          backgroundColor = RFColors.SocialTwitter,
            icon = Icons.Default.Share,
          onClick = { /* Twitter */ }
        )
        Spacer(modifier = Modifier.width(15.dp))
        RFSocialButton(
          backgroundColor = RFColors.SocialInstagram,
          icon = Icons.Default.FavoriteBorder,
          onClick = { /* Instagram */ }
        )
      }

      Spacer(modifier = Modifier.height(25.dp))

      // Reemplazo de RFInlineActionTextRow por dos líneas centradas
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = stringResource(id = R.string.dont_have_account_placeholder, "Don't have an account yet?"),
          color = RFColors.Subtitle,
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = stringResource(id = R.string.sign_up_action_placeholder, "Sign up."),
          color = RFColors.Link,
          fontSize = 13.sp,
          modifier = Modifier.clickable { onAction(LoginAction.OnRegisterClick) },
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Preview
@Composable
private fun LoginScreenPreview() {
  RFTheme {
    LoginScreen(
      state = LoginState(),
      onAction = { }
    )
  }
}