package com.example.auth.presentation.login.LoginView

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.presentation.designsystem.EmailIcon
import com.example.core.presentation.designsystem.LogoIcon
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.components.GradientBackground
import com.example.core.presentation.designsystem.components.RFActionButton
import com.example.core.presentation.designsystem.components.RFOutlinedActionButton
import com.example.core.presentation.designsystem.components.RFPasswordTextField
import com.example.core.presentation.designsystem.components.RFTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.text2.input.rememberTextFieldState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginRoute(
  onLogin: (email: String, password: String) -> Unit = { _, _ -> },
  onNavigateToSignUp: () -> Unit = {},
  onForgotPassword: () -> Unit = {}
) {
  val emailState = rememberTextFieldState()
  val passwordState = rememberTextFieldState()
  var passwordVisible by remember { mutableStateOf(false) }
  var isSubmitting by remember { mutableStateOf(false) }
  var showError by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()

  val canSubmit = remember(emailState.text, passwordState.text) {
    val email = emailState.text.toString()
    val pass = passwordState.text.toString()
    email.contains('@') && pass.length >= 6
  }

  LaunchedEffect(canSubmit) { if (showError && canSubmit) showError = false }

  GradientBackground(
    hasToolbar = false
  ) {
    Box(Modifier.fillMaxSize()) {
      DecorativeParticles()
      Column(
        modifier = Modifier
          .fillMaxSize()
          .systemBarsPadding()
          .padding(horizontal = 28.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        LogoBadge()
        Spacer(Modifier.height(24.dp))
        Text(
          "Rosa Fiesta",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
        )
        Spacer(Modifier.height(4.dp))
        Text(
          "Decora. Celebra. Inspira.",
          style = MaterialTheme.typography.bodyMedium.copy(
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(Modifier.height(36.dp))

        RFTextField(
          state = emailState,
          startIcon = EmailIcon,
          endIcon = null,
          hint = "correo@evento.com",
          title = "Correo",
          additionalInfo = if (!canSubmit && emailState.text.isNotEmpty() && !emailState.text.toString().contains('@')) "Correo inválido" else null,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        RFPasswordTextField(
          state = passwordState,
            hint = "Tu contraseña",
            title = "Contraseña",
            isVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            "¿Olvidaste tu contraseña?",
            style = MaterialTheme.typography.labelMedium.copy(
              color = MaterialTheme.colorScheme.primary,
              fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            ),
            modifier = Modifier.clickable { onForgotPassword() }
          )
          StrengthHint(passwordState.text.toString())
        }
        Spacer(Modifier.height(28.dp))

        RFActionButton(
          text = if (isSubmitting) "Ingresando..." else "Entrar",
          isLoading = isSubmitting,
          enabled = canSubmit && !isSubmitting,
          modifier = Modifier.fillMaxWidth(),
        ) {
          if (!canSubmit) {
            showError = true
            return@RFActionButton
          }
          scope.launch {
            isSubmitting = true
            // Simulación de llamada de red
            delay(1200)
            onLogin(emailState.text.toString(), passwordState.text.toString())
            isSubmitting = false
          }
        }
        AnimatedVisibility(showError) {
          Text(
            "Revisa tus datos (min 6 caracteres).",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 12.dp)
          )
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(20.dp))
        Text(
          "¿Primera vez celebrando con nosotros?",
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(Modifier.height(12.dp))
        RFOutlinedActionButton(
          text = "Crear cuenta",
          isLoading = false,
          modifier = Modifier.fillMaxWidth()
        ) { onNavigateToSignUp() }
        Spacer(Modifier.height(40.dp))
      }
    }
  }
}

@Composable
private fun LogoBadge() {
  val primary = MaterialTheme.colorScheme.primary
  val onPrimary = MaterialTheme.colorScheme.onPrimary
  val bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
  Box(
    modifier = Modifier
      .size(110.dp)
      .drawBehind {
        // halo
        drawCircle(
          Brush.radialGradient(
            listOf(primary.copy(alpha = 0.35f), Color.Transparent)
          ),
          radius = size.minDimension / 1.4f
        )
      }
      .clip(CircleShape)
      .background(
        Brush.linearGradient(
          listOf(primary, primary.copy(alpha = 0.6f))
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(90.dp)
        .clip(CircleShape)
        .background(bg)
        .borderGlow(primary)
        .padding(18.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = LogoIcon,
        contentDescription = null,
        tint = onPrimary.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}

private fun Modifier.borderGlow(color: Color, glowWidth: Dp = 4.dp): Modifier = this.drawBehind {
  val stroke = glowWidth.toPx()
  drawCircle(
    Brush.radialGradient(
      listOf(color.copy(alpha = 0.8f), Color.Transparent)
    ),
    radius = size.minDimension / 2f + stroke
  )
}

// Indicador muy simple de fuerza de contraseña
@Composable
private fun StrengthHint(pass: String) {
  if (pass.isEmpty()) return
  val strength = when {
    pass.length > 11 && pass.any { it.isDigit() } && pass.any { it.isUpperCase() } -> "Fuerte"
    pass.length > 7 -> "Media"
    else -> "Débil"
  }
  val color = when (strength) {
    "Fuerte" -> MaterialTheme.colorScheme.tertiary
    "Media" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.error
  }
  Text(
    strength,
    style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
    color = color
  )
}

// Partículas decorativas suaves (pétalos / confeti abstracto)
@Composable
private fun DecorativeParticles(count: Int = 14) {
  val infinite = rememberInfiniteTransition(label = "particles")
  val anims = List(count) { index ->
    val delayOffset = index * 150
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(6000, easing = FastOutSlowInEasing, delayMillis = delayOffset),
        repeatMode = RepeatMode.Reverse
      ), label = "p$index"
    )
  }
  val colors = listOf(
    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
    MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
  )
  Canvas(modifier = Modifier
    .fillMaxSize()
    .alpha(0.9f)) {
    val w = size.width
    val h = size.height
    val base = 40f
    anims.forEachIndexed { i, anim ->
      val t = anim.value
      val cx = (w / count) * i + (w / count) / 2f
      val cy = h * t
      val petalSize = base * (0.6f + (i % 5) * 0.12f)
      val rotation = (i * 25) + 180 * t
      withTransform({ this.rotate(rotation, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) }) {
        drawOval(
          color = colors[i % colors.size],
          topLeft = androidx.compose.ui.geometry.Offset(cx - petalSize / 2f, cy - petalSize / 3f),
          size = androidx.compose.ui.geometry.Size(petalSize, petalSize / 1.6f)
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
  RFTheme(darkTheme = false) {
    LoginRoute()
  }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreviewDark() {
  RFTheme(darkTheme = true) {
    LoginRoute()
  }
}
