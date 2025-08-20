@file:OptIn(ExperimentalFoundationApi::class)

package com.example.auth.presentation.login

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.presentation.R
import com.example.core.presentation.designsystem.*
import com.example.core.presentation.designsystem.components.*
import com.example.core.presentation.ui.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random

const val LOGIN_ROUTE = "login"

@Composable
fun LoginScreenRoot(
  onLoginSuccess: () -> Unit,
  onSignUpClick: () -> Unit,
  viewModel: LoginViewModel = koinViewModel()
) {
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

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

// Clase para representar una partícula
data class Particle(
    val id: Int,
    val initialX: Float,
    val initialY: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun LoginScreen(state: LoginState, onAction: (LoginAction) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(turquoise, brightPink),
                    startX = 0f,
                    endX = 1000f
                )
            )
    ) {
        // Efecto de partículas de fondo
        ParticleSystem()

        // Círculos decorativos de fondo
        DecorativeCircles()

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tarjeta principal del formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título
                    Text(
                        text = stringResource(id = R.string.hi_there),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = darkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Subtítulo
                    Text(
                        text = stringResource(id = R.string.runique_welcome_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = mediumGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Campo de email
                    RFTextField(
                        state = state.email,
                        startIcon = EmailIcon,
                        endIcon = null,
                        keyboardType = KeyboardType.Email,
                        hint = stringResource(id = R.string.example_email),
                        title = stringResource(id = R.string.email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de contraseña
                    RFPasswordTextField(
                        state = state.password,
                        onTogglePasswordVisibility = {
                            onAction(LoginAction.OnTogglePasswordVisibility)
                        },
                        isVisible = state.isPasswordVisible,
                        modifier = Modifier.fillMaxWidth(),
                        hint = stringResource(id = R.string.password),
                        title = stringResource(id = R.string.password),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de Sign In
                    RFActionButton(
                        text = stringResource(R.string.login),
                        isLoading = state.isLoggingIn,
                        enabled = state.canLogin && !state.isLoggingIn,
                        textColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        onAction(LoginAction.OnLoginClick)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Texto "Or sign in using:"
                    Text(
                        text = "Or sign in using:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mediumGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Iconos de redes sociales
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SocialIcon(
                            backgroundColor = facebookBlue,
                            onClick = { /* Facebook login */ }
                        ) {
                            Text(
                                text = "f",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        SocialIcon(
                            backgroundColor = twitterBlue,
                            onClick = { /* Twitter login */ }
                        ) {
                            Text(
                                text = "𝕏",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        SocialIcon(
                            backgroundColor = brightPink,
                            onClick = { /* Instagram login */ }
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Texto de registro
                    val signUpTag = "sign_up"
                    val dontHaveAccountText = stringResource(id = R.string.dont_have_an_account)
                    val signUpLabel = stringResource(id = R.string.sign_up)
                    val signUpText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontFamily = bodyFontFamily,
                                color = mediumGray
                            )
                        ) {
                            append(dontHaveAccountText + " ")
                            pushStringAnnotation(tag = signUpTag, annotation = "navigate")
                            withStyle(
                                style = SpanStyle(
                                    color = brightPink,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(signUpLabel)
                            }
                            pop()
                        }
                    }
                    ClickableText(
                        text = signUpText,
                        onClick = { offset ->
                            signUpText.getStringAnnotations(signUpTag, offset, offset)
                                .firstOrNull()?.let { onAction(LoginAction.OnRegisterClick) }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun DecorativeCircles() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Círculo grande superior derecho
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 100.dp, y = (-50).dp)
                .align(Alignment.TopEnd)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )

        // Círculo mediano superior derecho
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 200.dp, y = 150.dp)
                .align(Alignment.TopEnd)
                .background(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )

        // Círculo pequeño superior derecho
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 160.dp, y = 200.dp)
                .align(Alignment.TopEnd)
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )

        // Círculo grande inferior izquierdo
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-80).dp, y = 80.dp)
                .align(Alignment.BottomStart)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )

        // Círculo mediano inferior izquierdo
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = (-30).dp, y = (-20).dp)
                .align(Alignment.BottomStart)
                .background(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )

        // Círculo pequeño superior izquierdo
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-20).dp, y = 120.dp)
                .align(Alignment.TopStart)
                .background(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun SocialIcon(
    backgroundColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ParticleSystem() {
    val density = LocalDensity.current
    val screenWidth = with(density) { 400.dp.toPx() }
    val screenHeight = with(density) { 800.dp.toPx() }

    // Generar partículas aleatorias - todas empiezan desde abajo
    val particles = remember {
        (1..25).map { id ->
            Particle(
                id = id,
                initialX = Random.nextFloat() * screenWidth,
                initialY = screenHeight + 100f, // Todas empiezan desde abajo de la pantalla
                size = Random.nextFloat() * 8f + 4f,
                speed = Random.nextFloat() * 2000f + 3000f,
                alpha = Random.nextFloat() * 0.4f + 0.1f,
                color = if (Random.nextBoolean()) {
                    Color.White
                } else {
                    lightTurquoise // Color turquesa muy claro
                }
            )
        }
    }

    particles.forEach { particle ->
        AnimatedParticle(
            particle = particle,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }
}

@Composable
private fun AnimatedParticle(
    particle: Particle,
    screenWidth: Float,
    screenHeight: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_${particle.id}")

    // Animación de movimiento vertical - desde abajo hacia arriba continuamente
    val animatedY by infiniteTransition.animateFloat(
        initialValue = screenHeight + 100f, // Empieza desde abajo
        targetValue = -200f, // Sale por arriba
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = particle.speed.toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart // Se reinicia automáticamente
        ),
        label = "particle_y_${particle.id}"
    )

    // Animación de movimiento horizontal sutil (deriva)
    val animatedX by infiniteTransition.animateFloat(
        initialValue = particle.initialX - 50f,
        targetValue = particle.initialX + 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (particle.speed * 0.7f).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_x_${particle.id}"
    )

    // Animación de rotación sutil
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (particle.speed * 1.5f).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_rotation_${particle.id}"
    )

    // Animación de escala para efecto pulsante
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (particle.speed * 0.4f).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_scale_${particle.id}"
    )

    // Renderizar la partícula
    Box(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) { animatedX.toDp() },
                y = with(LocalDensity.current) { animatedY.toDp() }
            )
            .size(with(LocalDensity.current) { (particle.size * scale).toDp() })
            .graphicsLayer {
                rotationZ = rotation
                alpha = particle.alpha
            }
            .background(
                color = particle.color,
                shape = CircleShape
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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