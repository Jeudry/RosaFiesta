package com.example.auth.presentation.login.LoginView

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath

// Custom Icons
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
fun LoginView2() {
    var email by remember { mutableStateOf("johndoe@example.com") }
    var password by remember { mutableStateOf("••••••••") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF5EECF5), // Cyan
                        Color(0xFFFE709B)  // Pink
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            )
    ) {
        // Background Circles
        BackgroundShapes()

        // Status Bar
        StatusBar()

        // Login Form - Centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoginForm(
                email = email,
                password = password,
                isPasswordVisible = isPasswordVisible,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible }
            )
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

@Composable
fun BackgroundShapes() {
    // Left side circles
    Box(
        modifier = Modifier
            .size(80.dp)
            .offset(x = 30.dp, y = 450.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .blur(10.dp)
    )

    Box(
        modifier = Modifier
            .size(180.dp)
            .offset(x = (-30).dp, y = 300.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .blur(10.dp)
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .offset(x = (-100).dp, y = 600.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .blur(10.dp)
    )

    // Right side circles
    Box(
        modifier = Modifier
            .size(250.dp)
            .offset(x = 200.dp, y = (-80).dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .blur(10.dp)
    )


    Box(
        modifier = Modifier
            .size(60.dp)
            .offset(x = 320.dp, y = 400.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .blur(10.dp)
    )

    // Bottom right corner circles
    Box(
        modifier = Modifier
            .size(200.dp)
            .offset(x = 250.dp, y = 550.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .blur(10.dp)
    )

    Box(
        modifier = Modifier
            .size(90.dp)
            .offset(x = 300.dp, y = 650.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f))
            .blur(10.dp)
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .offset(x = 350.dp, y = 700.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .blur(10.dp)
    )



    Box(
        modifier = Modifier
            .size(140.dp)
            .offset(x = (-50).dp, y = 100.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .blur(10.dp)
    )
}

@Composable
fun LoginForm(
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit
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

            // Email field
            CustomTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Email",
                trailingIcon = Icons.Default.Check,
                trailingIconTint = Color(0xFF5EECF5),
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password field
            CustomTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                trailingIcon = if (isPasswordVisible) Icons.Default.Check else Icons.Default.Person,
                trailingIconTint = Color(0xFFBDC3C7),
                onTrailingIconClick = onPasswordVisibilityToggle,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Sign In Button
            Button(
                onClick = { /* Handle sign in */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFE709B)
                )
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Forgot Password
            Text(
                text = "Forgot password?",
                color = Color(0xFFFE709B),
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Handle forgot password */ }
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
                    modifier = Modifier.clickable { /* Handle sign up */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: ImageVector? = null,
    trailingIconTint: Color = Color.Gray,
    onTrailingIconClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        trailingIcon = {
            trailingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = trailingIconTint,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onTrailingIconClick?.invoke() }
                )
            }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF5EECF5),
            unfocusedBorderColor = Color(0xFFECF0F1),
            focusedTextColor = Color(0xFF2C3E50),
            unfocusedTextColor = Color(0xFF2C3E50)
        ),
        singleLine = true
    )
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

@Preview(showBackground = true)
@Composable
fun LoginView2Preview() {
    LoginView2()
}
