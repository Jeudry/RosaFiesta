@file:OptIn(ExperimentalFoundationApi::class)

package com.example.auth.presentation.login

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.presentation.R
import com.example.core.presentation.designsystem.EmailIcon
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.components.RFPasswordTextField
import com.example.core.presentation.designsystem.components.RFTextField
import com.example.core.presentation.ui.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random

@Suppress("unused")
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

@Composable
fun LoginScreen(state: LoginState, onAction: (LoginAction) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF5EECF5),
                        Color(0xFFFE709B)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        ParticleLayer()
        DecorativeBubbles()
        StatusBar()
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

val signalIcon: ImageVector by lazy {
    materialIcon(name = "Filled.SignalIcon") {
        materialPath {
            moveTo(2.0f, 17.0f)
            horizontalLineTo(6.0f)
            verticalLineTo(24.0f)
            horizontalLineTo(2.0f)
            close()
            moveTo(7.0f, 14.0f)
            horizontalLineTo(11.0f)
            verticalLineTo(24.0f)
            horizontalLineTo(7.0f)
            close()
            moveTo(12.0f, 11.0f)
            horizontalLineTo(16.0f)
            verticalLineTo(24.0f)
            horizontalLineTo(12.0f)
            close()
            moveTo(17.0f, 8.0f)
            horizontalLineTo(21.0f)
            verticalLineTo(24.0f)
            horizontalLineTo(17.0f)
            close()
        }
    }
}

val wifiIcon: ImageVector by lazy {
    materialIcon(name = "Filled.WifiIcon") {
        materialPath {
            moveTo(1.0f, 9.0f)
            lineToRelative(2.0f, 2.0f)
            curveToRelative(4.97f, -4.97f, 13.03f, -4.97f, 18.0f, 0.0f)
            lineToRelative(2.0f, -2.0f)
            curveTo(17.93f, 3.93f, 6.07f, 3.93f, 1.0f, 9.0f)
            close()
            moveTo(9.0f, 17.0f)
            lineToRelative(3.0f, 3.0f)
            lineToRelative(3.0f, -3.0f)
            curveToRelative(-1.65f, -1.66f, -4.34f, -1.66f, -6.0f, 0.0f)
            close()
            moveTo(5.0f, 13.0f)
            lineToRelative(2.0f, 2.0f)
            curveToRelative(2.76f, -2.76f, 7.24f, -2.76f, 10.0f, 0.0f)
            lineToRelative(2.0f, -2.0f)
            curveTo(15.14f, 9.14f, 8.87f, 9.14f, 5.0f, 13.0f)
            close()
        }
    }
}

val batteryIcon: ImageVector by lazy {
    materialIcon(name = "Filled.BatteryIcon") {
        materialPath {
            moveTo(15.67f, 4.0f)
            horizontalLineTo(14.0f)
            verticalLineTo(2.0f)
            horizontalLineTo(10.0f)
            verticalLineTo(4.0f)
            horizontalLineTo(8.33f)
            curveTo(7.6f, 4.0f, 7.0f, 4.6f, 7.0f, 5.33f)
            verticalLineTo(20.67f)
            curveTo(7.0f, 21.4f, 7.6f, 22.0f, 8.33f, 22.0f)
            horizontalLineTo(15.67f)
            curveTo(16.4f, 22.0f, 17.0f, 21.4f, 17.0f, 20.67f)
            verticalLineTo(5.33f)
            curveTo(17.0f, 4.6f, 16.4f, 4.0f, 15.67f, 4.0f)
            close()
            moveTo(15.0f, 20.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(6.0f)
            horizontalLineTo(15.0f)
            verticalLineTo(20.0f)
            close()
            moveTo(9.0f, 18.0f)
            horizontalLineTo(15.0f)
            verticalLineTo(16.0f)
            horizontalLineTo(9.0f)
            close()
            moveTo(9.0f, 14.0f)
            horizontalLineTo(15.0f)
            verticalLineTo(12.0f)
            horizontalLineTo(9.0f)
            close()
        }
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Signal and WiFi
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                signalIcon,
                contentDescription = "Signal",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Icon(
                wifiIcon,
                contentDescription = "WiFi",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Center - Battery
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "50%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                batteryIcon,
                contentDescription = "Battery",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Right side - Time
        Text(
            text = "12:30",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Bubble(
    val xFraction: Float,
    val yFraction: Float,
    val radius: Dp,
    val alpha: Float
)

@Composable
private fun DecorativeBubbles() {
    val bubbles = listOf(
        Bubble(0.12f, 0.25f, 70.dp, 0.20f),
        Bubble(0.85f, 0.30f, 110.dp, 0.18f),
        Bubble(0.12f, 0.75f, 55.dp, 0.17f),
        Bubble(0.92f, 0.70f, 42.dp, 0.15f),
        Bubble(0.30f, 0.90f, 90.dp, 0.14f)
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        bubbles.forEach { b ->
            val radius = b.radius.toPx()
            val center = Offset(size.width * b.xFraction, size.height * b.yFraction)
            drawCircle(
                color = Color.White.copy(alpha = b.alpha),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = b.alpha * 0.55f),
                radius = radius,
                center = center,
                style = Stroke(width = radius * 0.05f)
            )
        }
    }
}

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
            // Title
            Text(
                text = "Sign In",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.fillMaxWidth()
            )

            // Subtitle
            Text(
                text = "Please sign in to your account first",
                fontSize = 14.sp,
                color = Color(0xFF7F8C8D),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp, top = 8.dp),
                lineHeight = 20.sp
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

            Spacer(modifier = Modifier.height(30.dp))

            // Sign In Button
            Button(
                onClick = { onAction(LoginAction.OnLoginClick) },
                enabled = state.canLogin && !state.isLoggingIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.canLogin) Color(0xFFFE709B) else Color(0x55FE709B),
                    disabledContainerColor = Color(0x33FE709B)
                )
            ) {
                if (state.isLoggingIn) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Forgot Password
            Text(
                text = "Forgot password?",
                color = Color(0xFFFE709B),
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO */ }
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Divider
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    color = Color(0xFFECF0F1),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Or sign in using:",
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier
                        .background(Color.White)
                        .padding(horizontal = 15.dp)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Social Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SocialButton(
                    backgroundColor = Color(0xFF3B5998),
                    icon = Icons.Default.Person, // Facebook placeholder
                    onClick = { /* Handle Facebook login */ }
                )

                Spacer(modifier = Modifier.width(15.dp))

                SocialButton(
                    backgroundColor = Color(0xFF1DA1F2),
                    icon = Icons.Default.Share, // Twitter placeholder
                    onClick = { /* Handle Twitter login */ }
                )

                Spacer(modifier = Modifier.width(15.dp))

                SocialButton(
                    backgroundColor = Color(0xFFE4405F),
                    icon = Icons.Default.FavoriteBorder, // Instagram placeholder
                    onClick = { /* Handle Instagram login */ }
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Sign Up Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account yet? ",
                    color = Color(0xFF7F8C8D),
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign up.",
                    color = Color(0xFFFE709B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onAction(LoginAction.OnRegisterClick) }
                )
            }
        }
    }
}

@Composable
fun SocialButton(
    backgroundColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(45.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

private data class Particle2(
    val id: Int,
    val initialX: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val color: Color
)

@Composable
private fun ParticleLayer(count: Int = 50) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Box(Modifier.fillMaxSize().onGloballyPositioned { size = it.size }) {
        if (size.width > 0 && size.height > 0) {
            ParticleSystem2(
                screenWidth = size.width.toFloat(),
                screenHeight = size.height.toFloat(),
                count = count
            )
        }
    }
}

@Composable
private fun ParticleSystem2(screenWidth: Float, screenHeight: Float, count: Int = 60) { // antes 26
    // Re-crear partículas si cambia el tamaño
    val particles = remember(screenWidth, screenHeight, count) {
        (1..count).map { id ->
            val r = Random.nextFloat()
            val sizePx = when {
                r < 0.07f -> Random.nextFloat() * 28f + 22f   // grandes destacadas
                r < 0.32f -> Random.nextFloat() * 14f + 10f   // medianas
                else -> Random.nextFloat() * 8f + 4f          // pequeñas
            }
            val speedBase = when {
                sizePx > 30f -> Random.nextFloat() * 3000f + 3600f  // grandes más lentas
                sizePx > 18f -> Random.nextFloat() * 2600f + 3000f
                else -> Random.nextFloat() * 2200f + 2600f
            }
            Particle2(
                id = id,
                initialX = Random.nextFloat() * screenWidth,
                speed = speedBase,
                size = sizePx,
                alpha = if (sizePx > 30f) Random.nextFloat() * 0.30f + 0.30f else Random.nextFloat() * 0.35f + 0.15f,
                color = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.50f),
                    Color(0xCCFFFFFF)
                ).random()
            )
        }
    }
    particles.forEach { p -> AnimatedParticle2(p, screenHeight) }
}

@Composable
private fun AnimatedParticle2(particle: Particle2, screenHeight: Float) {
    val density = LocalDensity.current
    val infinite = rememberInfiniteTransition(label = "p_${'$'}{particle.id}")
    val y by infinite.animateFloat(
        initialValue = screenHeight + 140f,
        targetValue = -220f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = particle.speed.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "py_${'$'}{particle.id}"
    )
    val x by infinite.animateFloat(
        initialValue = particle.initialX - 70f,
        targetValue = particle.initialX + 70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (particle.speed * 0.7f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "px_${'$'}{particle.id}"
    )
    val scale by infinite.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (particle.speed * 0.45f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ps_${'$'}{particle.id}"
    )
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (particle.speed * 1.4f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pr_${'$'}{particle.id}"
    )
    Box(
        modifier = Modifier
            .offset(
                x = with(density) { x.toDp() },
                y = with(density) { y.toDp() }
            )
            .size(with(density) { (particle.size * scale).toDp() })
            .graphicsLayer { this.rotationZ = rotation; this.alpha = particle.alpha }
            .background(particle.color, shape = CircleShape)
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