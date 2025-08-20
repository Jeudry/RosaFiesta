package com.example.auth.presentation.login.LoginView

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Login2() {
    // Paleta de colores (autocontenida)
    val aqua = Color(0xFF29D7E8)
    val pink = Color(0xFFFF4F8B)
    val darkText = Color(0xFF222222)
    val hint = Color(0xFF777B85)
    val cardBackground = Color.White
    val accent = pink

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(listOf(aqua, pink)))
    ) {
        // Burbujas decorativas
        DecorativeBubbles()

        // Contenido principal centrado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginCard(
                aqua = aqua,
                pink = pink,
                darkText = darkText,
                hint = hint,
                cardBackground = cardBackground,
                accent = accent
            )
        }
    }
}

@Composable
private fun LoginCard(
    aqua: Color,
    pink: Color,
    darkText: Color,
    hint: Color,
    cardBackground: Color,
    accent: Color
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val emailValid = remember(email) {
        // Validación básica
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val waveHeight = 110.dp

    Box(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBackground)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Encabezado con forma ondulada
            WaveHeader(height = waveHeight, aqua = aqua, pink = pink)
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Please sign in to your account first",
                    fontSize = 13.sp,
                    color = hint
                )
                Spacer(Modifier.height(28.dp))

                // Email (Material3)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                    trailingIcon = {
                        if (emailValid) {
                            // Indicador simple de validación
                            Canvas(Modifier.size(20.dp)) {
                                drawCircle(color = aqua)
                                val check = Path().apply {
                                    moveTo(size.width * 0.28f, size.height * 0.55f)
                                    lineTo(size.width * 0.45f, size.height * 0.72f)
                                    lineTo(size.width * 0.75f, size.height * 0.32f)
                                }
                                drawPath(check, color = Color.White, style = Stroke(width = size.minDimension * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        cursorColor = accent,
                        focusedLabelColor = accent
                    )
                )
                Spacer(Modifier.height(20.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            if (passwordVisible) "Hide" else "Show",
                            color = hint,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        cursorColor = accent,
                        focusedLabelColor = accent
                    )
                )
                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = { /* acción login */ },
                    enabled = emailValid && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        disabledContainerColor = accent.copy(alpha = 0.35f)
                    )
                ) {
                    Text("Sign In", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Forgot password?",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { },
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Or sign in using:",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    color = hint,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                SocialRow(aqua = aqua, pink = pink)
                Spacer(Modifier.height(24.dp))

                val annotated = buildAnnotatedString {
                    withStyle(SpanStyle(color = hint, fontSize = 12.sp)) { append("Don't have an account yet? ") }
                    withStyle(SpanStyle(color = accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append("Sign up.") }
                }
                Text(
                    text = annotated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { },
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(26.dp))
            }
        }
    }
}

@Composable
private fun WaveHeader(height: Dp, aqua: Color, pink: Color) {
    val hPx = with(LocalDensity.current) { height.toPx() }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Fondo blanco para continuidad
        drawRect(color = Color.White, size = Size(size.width, size.height))

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, hPx * 0.55f)
            // Curva ondulada hacia la izquierda
            quadraticBezierTo(
                size.width * 0.75f, hPx * 0.25f,
                size.width * 0.52f, hPx * 0.42f
            )
            quadraticBezierTo(
                size.width * 0.35f, hPx * 0.55f,
                size.width * 0.15f, hPx * 0.35f
            )
            quadraticBezierTo(
                size.width * 0.05f, hPx * 0.25f,
                0f, hPx * 0.38f
            )
            close()
        }
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(aqua, pink))
        )
    }
}

@Composable
private fun SocialRow(aqua: Color, pink: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialCircle(label = "f", background = Color(0xFF1877F2), content = Color.White)
        SocialCircle(label = "t", background = Color(0xFF1DA1F2), content = Color.White)
        SocialCircle(label = "ig", background = Brush.radialGradient(listOf(pink, Color(0xFFFFC107), aqua)))
    }
}

@Composable
private fun SocialCircle(
    label: String,
    background: Any, // Color o Brush
    content: Color = Color.White
) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(
                when (background) {
                    is Color -> background
                    is Brush -> Color.Transparent
                    else -> Color.Gray
                }
            )
            .let { base ->
                if (background is Brush) base.background(background) else base
            }
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(label.uppercase(), color = content, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

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

private data class Bubble(
    val xFraction: Float,
    val yFraction: Float,
    val radius: Dp,
    val alpha: Float
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewLogin2() {
    Login2()
}