package com.example.auth.presentation.login.LoginView

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.min
import kotlin.random.Random
import androidx.compose.ui.graphics.lerp

@Composable
fun Login1(onLogin: (email: String, password: String, remember: Boolean) -> Unit = { _, _, _ -> }) {
    // Nueva paleta rosa refinada (menos blanco arriba, transición más rica)
    val gradientTop = listOf(
        Color(0xFFFFE4EF), // más etéreo
        Color(0xFFFFB6D2),
        Color(0xFFFF63A8)
    )
    // Fondo aqua pastel suavizado (antes más saturado)
    val baseCyanLight = Color(0xFFDDFBFE) // pastel aireado
    val baseCyanMid = Color(0xFF9EE8F1)   // transición suave
    val primaryColor = Color(0xFFF0448C)
    val accentText = primaryColor
    val checkboxChecked = primaryColor
    val labelColor = Color(0xFF7A3352)
    val subtleBorder = Color(0xFFF5D9E5)
    val cardShape = RoundedCornerShape(24.dp)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    val pink = Color(0xFFFF4F8B)
    val aqua = baseCyanMid

    Box(modifier = Modifier.fillMaxSize().background(baseCyanMid)) {
        // Fondo diagonal (sin bordes redondeados) + partículas
        DiagonalPinkBackground(gradientTop, baseCyanLight, baseCyanMid)
        ParticleSystem()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(96.dp))
            BasicText(
                text = "Login",
                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            )
            BasicText(
                text = "Por favor inicia sesión para continuar",
                style = TextStyle(fontSize = 13.sp, color = Color(0xFFFFECF3)),
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(44.dp))

            // Card flotante: parte superior en azul, inferior en blanco
            Box(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .shadow(elevation = 12.dp, shape = cardShape, clip = false)
                    .clip(cardShape)
                    .background(Color.White)
            ) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 26.dp)) {
                    FieldLabel("Email Address", labelColor)
                    Spacer(Modifier.height(6.dp))
                    InputField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "email@gmail.com",
                        keyboardIsPassword = false,
                        showPassword = false,
                        onTogglePassword = { }
                    )
                    Spacer(Modifier.height(18.dp))
                    FieldLabel("Password", labelColor)
                    Spacer(Modifier.height(6.dp))
                    InputField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "********",
                        keyboardIsPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        CustomCheckbox(checked = rememberMe, onCheckedChange = { rememberMe = it }, checkedColor = checkboxChecked)
                        BasicText("Recordarme", style = TextStyle(fontSize = 12.sp, color = Color.Black), modifier = Modifier.padding(start = 6.dp))
                        Spacer(Modifier.weight(1f))
                        BasicText(
                            text = "¿Olvidaste tu contraseña?",
                            style = TextStyle(fontSize = 12.sp, color = accentText),
                            modifier = Modifier.clickable { }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    val loginEnabled = email.isNotBlank() && password.isNotBlank()
                    Button(
                        onClick = { onLogin(email, password, rememberMe) },
                        enabled = loginEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        val gradientEnabled = remember(pink, aqua) {
                            val c2 = lerp(pink, aqua, 0.35f)
                            val c3 = lerp(pink, aqua, 0.55f)
                            val c4 = lerp(pink, aqua, 0.75f)
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.00f to pink.copy(alpha = 0.95f),
                                    0.25f to c2.copy(alpha = 0.93f),
                                    0.55f to c3.copy(alpha = 0.92f),
                                    0.85f to c4.copy(alpha = 0.94f),
                                    1.00f to aqua.copy(alpha = 0.98f)
                                )
                            )
                        }
                        val gradientDisabled = remember(pink, aqua) {
                            val c2 = lerp(pink, aqua, 0.35f)
                            val c3 = lerp(pink, aqua, 0.55f)
                            val c4 = lerp(pink, aqua, 0.75f)
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.00f to pink.copy(alpha = 0.35f),
                                    0.25f to c2.copy(alpha = 0.30f),
                                    0.55f to c3.copy(alpha = 0.28f),
                                    0.85f to c4.copy(alpha = 0.30f),
                                    1.00f to aqua.copy(alpha = 0.34f)
                                )
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (loginEnabled) gradientEnabled else gradientDisabled)
                        ) {
                            // Highlight suave superior
                            Canvas(Modifier.matchParentSize()) {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.18f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = size.height * 0.60f
                                    ),
                                    size = Size(size.width, size.height * 0.60f)
                                )
                            }
                            BasicText(
                                text = "Login",
                                modifier = Modifier.align(Alignment.Center),
                                style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                    Spacer(Modifier.height(26.dp))
                    OrDivider()
                    Spacer(Modifier.height(18.dp))
                    SocialRow()
                    Spacer(Modifier.height(20.dp))
                    SignUpText(onSignup = { }, accentColor = accentText)
                }
            }

            Spacer(Modifier.height(56.dp))
            BiometricSection(onClick = { /* acción biometría */ }, color = Color(0xFF1E64D5), borderColor = Color.Transparent, iconSize = 44.dp)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// Fondo diagonal rosa
@Composable
private fun DiagonalPinkBackground(gradientColors: List<Color>, baseTop: Color = Color.White, baseBottom: Color = Color.White) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Fondo base degradado
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(baseTop, baseBottom),
                startY = 0f,
                endY = size.height
            )
        )
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.56f)
            lineTo(0f, size.height * 0.43f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = gradientColors,
                start = Offset(x = size.width * 0.5f, y = 0f),
                end = Offset(x = size.width * 0.5f, y = size.height * 0.65f)
            )
        )
        // Halo central suave para un look más "soft"
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.58f),
                radius = size.minDimension * 0.85f
            ),
            radius = size.minDimension * 0.85f,
            center = Offset(size.width * 0.5f, size.height * 0.58f)
        )
        // Círculos decorativos translúcidos
        val circles = listOf(
            Triple(0.18f, 0.18f, size.minDimension * 0.17f),
            Triple(0.82f, 0.12f, size.minDimension * 0.22f),
            Triple(0.10f, 0.44f, size.minDimension * 0.12f),
            Triple(0.90f, 0.47f, size.minDimension * 0.10f),
            Triple(0.63f, 0.30f, size.minDimension * 0.14f)
        )
        circles.forEach { (xf, yf, r) ->
            val center = Offset(size.width * xf, size.height * yf)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.05f), Color.Transparent),
                    center = center,
                    radius = r * 1.4f
                ),
                radius = r * 1.4f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = r,
                center = center
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, color: Color = Color(0xFF0C2A52)) {
    BasicText(text = text, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color))
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardIsPassword: Boolean,
    showPassword: Boolean,
    onTogglePassword: () -> Unit
) {
    val bg = Color(0xFFFFF4F9) // tono aún más claro
    val textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF5A2F47))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.CenterStart
    ) {
        var internalText by remember { mutableStateOf(value) }
        // Sync external state
        if (internalText != value) internalText = value
        BasicTextField(
            value = internalText,
            onValueChange = {
                internalText = it
                onValueChange(it)
            },
            singleLine = true,
            textStyle = textStyle,
            visualTransformation = if (keyboardIsPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = if (keyboardIsPassword) 44.dp else 14.dp)
        )
        if (internalText.isEmpty()) {
            BasicText(
                text = placeholder,
                style = textStyle.copy(color = Color(0xFF9AA4B4)) ,
                modifier = Modifier.padding(start = 14.dp)
            )
        }
        if (keyboardIsPassword) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(28.dp)
                    .clickable { onTogglePassword() },
                contentAlignment = Alignment.Center
            ) {
                EyeIcon(open = showPassword, tint = Color(0xFFF0448C))
            }
        }
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        DividerLine()
        BasicText("  o continúa con:  ", style = TextStyle(fontSize = 12.sp, color = Color(0xFF4A4F57)))
        DividerLine()
    }
}

@Composable
private fun RowScope.DividerLine() {
    Box(
        modifier = Modifier
            .height(1.dp)
            .weight(1f)
            .background(Color(0xFFE2E6EB))
    )
}

@Composable
private fun SocialRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        SocialButton(label = "G", background = Color.White, border = Color(0xFFE0E5EA), logoColor = Color(0xFFDB4437))
        SocialButton(label = "f", background = Color.White, border = Color(0xFFE0E5EA), logoColor = Color(0xFF1877F2))
        SocialButton(label = "\uD83D\uDD43", background = Color.White, border = Color(0xFFE0E5EA), logoColor = Color(0xFF1DA1F2))
    }
}

@Composable
private fun SocialButton(label: String, background: Color, border: Color, logoColor: Color) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(14.dp))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        BasicText(label, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = logoColor))
    }
}

@Composable
private fun SignUpText(onSignup: () -> Unit, accentColor: Color = Color(0xFFE24986)) {
    val tag = "signup_tag"
    val text: AnnotatedString = buildAnnotatedString {
        append("¿No tienes una cuenta? ")
        pushStringAnnotation(tag = tag, annotation = "signup")
        withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold)) { append("Regístrate") }
        pop()
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ClickableText(
            text = text,
            style = TextStyle(fontSize = 12.sp, color = Color(0xFF4A4F57), textAlign = TextAlign.Center),
            onClick = { offset -> text.getStringAnnotations(tag, offset, offset).firstOrNull()?.let { onSignup() } }
        )
    }
}

@Composable
private fun CustomCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, checkedColor: Color = Color(0xFFF0448C)) {
    val shape = RoundedCornerShape(4.dp)
    val sizeBox = 20.dp
    val borderColor = if (checked) checkedColor else Color(0xFFE9C5D3)
    Box(
        modifier = Modifier
            .size(sizeBox)
            .clip(shape)
            .border(1.5.dp, borderColor, shape)
            .background(if (checked) checkedColor else Color.Transparent)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                val stroke = Stroke(width = size.minDimension * 0.15f, cap = StrokeCap.Round)
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.55f)
                    lineTo(size.width * 0.42f, size.height * 0.75f)
                    lineTo(size.width * 0.78f, size.height * 0.28f)
                }
                drawPath(path = path, color = Color.White, style = stroke)
            }
        }
    }
}

@Composable
private fun EyeIcon(open: Boolean, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        // Ojo (almendra)
        val path = Path().apply {
            moveTo(0f, h * 0.5f)
            quadraticBezierTo(w * 0.5f, -h * 0.2f, w, h * 0.5f)
            quadraticBezierTo(w * 0.5f, h * 1.2f, 0f, h * 0.5f)
            close()
        }
        drawPath(path, color = tint.copy(alpha = 0.12f))
        drawPath(path, color = tint, style = stroke)
        if (open) {
            drawCircle(color = tint, radius = w * 0.16f, center = center)
            drawCircle(color = Color.White, radius = w * 0.07f, center = center)
        } else {
            // Línea diagonal para cerrado
            drawLine(tint, start = Offset(w * 0.15f, h * 0.8f), end = Offset(w * 0.85f, h * 0.2f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun BiometricSection(onClick: () -> Unit, color: Color = Color(0xFFF0448C), borderColor: Color = Color(0xFFF0448C), iconSize: Dp = 34.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Transparent)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            FingerPrintIllustration(size = iconSize, color = color)
        }
        Spacer(Modifier.height(2.dp))
        BasicText(
            text = "Usar biometría",
            style = TextStyle(fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun FingerPrintIllustration(size: Dp, color: Color) {
    val stroke = with(LocalDensity.current) { 3.dp.toPx() }
    Canvas(modifier = Modifier.size(size)) {
        val w = min(this.size.width, this.size.height)
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radii = listOf(0.48f, 0.38f, 0.28f, 0.18f)
        radii.forEachIndexed { i, r ->
            drawArc(
                color = color,
                startAngle = 200f - i * 6f,
                sweepAngle = 220f + i * 10f,
                useCenter = false,
                topLeft = Offset(center.x - w * r, center.y - w * r),
                size = Size(w * r * 2, w * r * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        drawLine(
            color = color,
            start = Offset(center.x, center.y + w * 0.05f),
            end = Offset(center.x, center.y + w * 0.32f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

// --- Partículas (copiadas y adaptadas) ---
private data class Particle(
    val id: Int,
    val initialX: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val color: Color
)

@Composable
private fun ParticleSystem() {
    val density = LocalDensity.current
    // Usamos tamaño aproximado; al ser decorativo basta un área grande
    val screenWidth = with(density) { 400.dp.toPx() }
    val screenHeight = with(density) { 900.dp.toPx() }
    val particles = remember {
        (1..28).map { id ->
            Particle(
                id = id,
                initialX = Random.nextFloat() * screenWidth,
                speed = Random.nextFloat() * 2200f + 2600f,
                size = Random.nextFloat() * 8f + 4f,
                alpha = Random.nextFloat() * 0.35f + 0.15f,
                color = if (Random.nextBoolean()) Color.White else Color(0x66FFFFFF)
            )
        }
    }
    particles.forEach { p -> AnimatedParticle(p, screenWidth, screenHeight) }
}

@Composable
private fun AnimatedParticle(particle: Particle, screenWidth: Float, screenHeight: Float) {
    val infinite = rememberInfiniteTransition(label = "p_${'$'}{particle.id}")
    val y by infinite.animateFloat(
        initialValue = screenHeight + 120f,
        targetValue = -200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = particle.speed.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "py_${'$'}{particle.id}"
    )
    val x by infinite.animateFloat(
        initialValue = particle.initialX - 60f,
        targetValue = particle.initialX + 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (particle.speed * 0.7f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "px_${'$'}{particle.id}"
    )
    val scale by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
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
                x = with(LocalDensity.current) { x.toDp() },
                y = with(LocalDensity.current) { y.toDp() }
            )
            .size(with(LocalDensity.current) { (particle.size * scale).toDp() })
            .graphicsLayer { this.rotationZ = rotation; this.alpha = particle.alpha }
            .background(particle.color, shape = CircleShape)
    )
}
// --- Fin partículas ---

@Preview()
@Composable
private fun Login1Preview() { Login1() }
