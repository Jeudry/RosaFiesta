package com.example.products.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.products.presentation.model.CategoryUi

import androidx.compose.animation.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


@Composable
fun CategoriesList(
    modifier: Modifier = Modifier,
    categoryList: List<CategoryUi> = emptyList(),
) {
    var selectedCategoryId by remember { mutableStateOf(categoryList.first().id) }

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoryList) { category ->
            CategoryItem(
                name = category.name,
                isSelected = category.id == selectedCategoryId
            ) {
                selectedCategoryId = category.id
            }
        }
    }
}

@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        modifier = modifier
            .background(color = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp))
            .clickable(
                onClick = {
                    onClick.invoke()
                }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        text = name,
        fontSize = 16.sp,
        color = if (isSelected)
            MaterialTheme.colorScheme.onSecondaryContainer
        else
            MaterialTheme.colorScheme.onSurface,
        style = TextStyle(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun CategoriesListPreview() {
    CategoriesList(
        modifier = Modifier.padding(6.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryItemPreview() {
    CategoryItem(
        modifier = Modifier.padding(4.dp),
        name = "Sneakers",
        isSelected = true,
        onClick = {}
    )
}


// Colores de la paleta de PartyDeco
object PartyDecoTheme {
    val primaryGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2)
        )
    )

    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2),
            Color(0xFFF093FB)
        )
    )

    val partyColors = listOf(
        Color(0xFFFF6B6B), // Rojo coral
        Color(0xFF4ECDC4), // Turquesa
        Color(0xFF45B7D1), // Azul
        Color(0xFFF9CA24), // Amarillo
        Color(0xFFF0932B)  // Naranja
    )
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null,
    val isLoginSuccessful: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    data class RememberMeChanged(val remember: Boolean) : LoginEvent()
    object Login : LoginEvent()
    object GoogleLogin : LoginEvent()
    object FacebookLogin : LoginEvent()
    object ForgotPassword : LoginEvent()
    object SignUp : LoginEvent()
    object DismissError : LoginEvent()
}

@Preview(showBackground = true)
@Composable
fun PartyDecoLoginScreenPreview() {
    PartyDecoLoginScreen(
        onLoginSuccess = {},
        onNavigateToSignUp = {},
        onForgotPassword = {},
        onGoogleLogin = {},
        onFacebookLogin = {}
    )
}

@Composable
fun PartyDecoLoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onGoogleLogin: () -> Unit = {},
    onFacebookLogin: () -> Unit = {}
) {
    var loginState by remember { mutableStateOf(LoginState()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Focus requesters
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    // Función para manejar eventos
    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                loginState = loginState.copy(
                    email = event.email,
                    emailError = null
                )
            }
            is LoginEvent.PasswordChanged -> {
                loginState = loginState.copy(
                    password = event.password,
                    passwordError = null
                )
            }
            LoginEvent.TogglePasswordVisibility -> {
                loginState = loginState.copy(
                    isPasswordVisible = !loginState.isPasswordVisible
                )
            }
            is LoginEvent.RememberMeChanged -> {
                loginState = loginState.copy(rememberMe = event.remember)
            }
            LoginEvent.Login -> {
                performLogin(loginState) { newState ->
                    loginState = newState
                    if (newState.isLoginSuccessful) {
                        onLoginSuccess()
                    }
                }
            }
            LoginEvent.GoogleLogin -> onGoogleLogin()
            LoginEvent.FacebookLogin -> onFacebookLogin()
            LoginEvent.ForgotPassword -> onForgotPassword()
            LoginEvent.SignUp -> onNavigateToSignUp()
            LoginEvent.DismissError -> {
                loginState = loginState.copy(
                    emailError = null,
                    passwordError = null,
                    loginError = null
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PartyDecoTheme.backgroundGradient)
    ) {
        // Animaciones de fondo
        PartyBackgroundAnimations()

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo y título
            PartyDecoHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Formulario de login
            LoginForm(
                state = loginState,
                onEvent = ::handleEvent,
                emailFocusRequester = emailFocusRequester,
                passwordFocusRequester = passwordFocusRequester,
                keyboardController = keyboardController
            )
        }

        // Snackbar para errores
        loginState.loginError?.let { error ->
            LaunchedEffect(error) {
                delay(3000)
                handleEvent(LoginEvent.DismissError)
            }
        }
    }
}

@Composable
private fun PartyBackgroundAnimations() {
    // Partículas de confetti animadas
    repeat(8) { index ->
        val animationDelay = (index * 500).toLong()
        ConfettiParticle(
            color = PartyDecoTheme.partyColors[index % PartyDecoTheme.partyColors.size],
            animationDelay = animationDelay
        )
    }

    // Globos flotantes
    repeat(3) { index ->
        FloatingBalloon(
            color = PartyDecoTheme.partyColors[index],
            index = index
        )
    }
}

@Composable
private fun ConfettiParticle(
    color: Color,
    animationDelay: Long
) {
    var isVisible by remember { mutableStateOf(false) }
    val startX = remember { Random.nextFloat() }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        isVisible = true
        delay(3000)
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .offset(x = (startX * 300).dp, y = (-50).dp)
                .size(8.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
private fun FloatingBalloon(
    color: Color,
    index: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "balloon_animation")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000 + (index * 500),
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balloon_float"
    )

    Box(
        modifier = Modifier
            .offset(
                x = (50 + index * 100).dp,
                y = (100 + index * 50 + offsetY).dp
            )
            .size(width = 20.dp, height = 30.dp)
            .background(
                color,
                RoundedCornerShape(
                    topStart = 50.dp,
                    topEnd = 50.dp,
                    bottomStart = 25.dp,
                    bottomEnd = 25.dp
                )
            )
    )
}

@Composable
private fun PartyDecoHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo con animación
        var isLogoVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isLogoVisible = true
        }

        AnimatedVisibility(
            visible = isLogoVisible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "PartyDeco Logo",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "PartyDeco",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Haz realidad cada celebración",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginForm(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    emailFocusRequester: FocusRequester,
    passwordFocusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo de email
            PartyDecoTextField(
                value = state.email,
                onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                isError = state.emailError != null,
                errorMessage = state.emailError,
                modifier = Modifier.focusRequester(emailFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            PartyDecoTextField(
                value = state.password,
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                label = "Contraseña",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (state.isPasswordVisible) Icons.Default.Search else Icons.Default.Call,
                onTrailingIconClick = { onEvent(LoginEvent.TogglePasswordVisibility) },
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onEvent(LoginEvent.Login)
                    }
                ),
                isError = state.passwordError != null,
                errorMessage = state.passwordError,
                modifier = Modifier.focusRequester(passwordFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Opciones del formulario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.rememberMe,
                        onCheckedChange = { onEvent(LoginEvent.RememberMeChanged(it)) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF667EEA)
                        )
                    )
                    Text(
                        text = "Recordarme",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }

                TextButton(
                    onClick = { onEvent(LoginEvent.ForgotPassword) }
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        fontSize = 14.sp,
                        color = Color(0xFF667EEA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de login
            PartyDecoButton(
                text = "Iniciar Sesión",
                onClick = { onEvent(LoginEvent.Login) },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divisor
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    text = " o continúa con ",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de redes sociales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialLoginButton(
                    text = "Google",
                    onClick = { onEvent(LoginEvent.GoogleLogin) },
                    modifier = Modifier.weight(1f)
                )

                SocialLoginButton(
                    text = "Facebook",
                    onClick = { onEvent(LoginEvent.FacebookLogin) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link de registro
            Row {
                Text(
                    text = "¿No tienes cuenta? ",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )

                Text(
                    text = "Regístrate aquí",
                    fontSize = 14.sp,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onEvent(LoginEvent.SignUp) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartyDecoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color(0xFF667EEA)
                )
            },
            trailingIcon = trailingIcon?.let { icon ->
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF666666)
                        )
                    }
                }
            },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE1E5E9)
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun PartyDecoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PartyDecoTheme.primaryGradient, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E5E9)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White
        )
    ) {
        Text(
            text = text,
            color = Color(0xFF333333),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Función para simular el proceso de login
private fun performLogin(
    state: LoginState,
    onStateChange: (LoginState) -> Unit
) {
    // Validaciones
    val emailError = when {
        state.email.isBlank() -> "Por favor ingresa tu email"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Por favor ingresa un email válido"
        else -> null
    }

    val passwordError = when {
        state.password.isBlank() -> "Por favor ingresa tu contraseña"
        state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    if (emailError != null || passwordError != null) {
        onStateChange(
            state.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        )
        return
    }

    // Iniciar carga
    onStateChange(state.copy(isLoading = true))

    // Simular llamada a API
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        delay(2000) // Simular delay de red

        if (state.email == "demo@partydeco.com" && state.password == "demo123") {
            onStateChange(
                state.copy(
                    isLoading = false,
                    isLoginSuccessful = true
                )
            )
        } else {
            onStateChange(
                state.copy(
                    isLoading = false,
                    loginError = "Email o contraseña incorrectos"
                )
            )
        }
    }
}
