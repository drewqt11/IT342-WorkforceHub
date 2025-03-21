package cit.edu.workforce

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.workforce.R

private val montserratFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_black, FontWeight.Black)
)

@Composable
fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val labelScale by animateFloatAsState(
        targetValue = if (isFocused || value.isNotEmpty()) 0.75f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val containerElevation by animateFloatAsState(
        targetValue = if (isFocused) 4f else 2f,
        animationSpec = tween(durationMillis = 200)
    )
    
    val containerAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.08f else 0.04f,
        animationSpec = tween(durationMillis = 300)
    )

    val primaryColor = Color(0xFF32747E)

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                text = label,
                color = if (isFocused) primaryColor else Color.Gray,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer {
                    scaleX = labelScale
                    scaleY = labelScale
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
            ) 
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .graphicsLayer {
                shadowElevation = containerElevation
            },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        interactionSource = interactionSource,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = primaryColor.copy(alpha = containerAlpha),
            unfocusedContainerColor = Color.Gray.copy(alpha = containerAlpha),
            focusedIndicatorColor = primaryColor,
            unfocusedIndicatorColor = Color.LightGray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.DarkGray,
            cursorColor = primaryColor,
            focusedLabelColor = primaryColor,
            unfocusedLabelColor = Color.Gray
        ),
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(
            fontFamily = montserratFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedButtons(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color = Color.White,
    text: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontFamily = montserratFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val gradientColors = listOf(
        Color(0xFF1F1F1F),
        Color(0xFF312F2F)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Welcome back!",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 40.sp,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Sign in to continue",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 20.sp,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .defaultMinSize(minHeight = 400.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text(
                                text = "Email",
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF32747E),
                            focusedLabelColor = Color(0xFF32747E),
                            cursorColor = Color(0xFF32747E)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                text = "Password",
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF32747E),
                            focusedLabelColor = Color(0xFF32747E),
                            cursorColor = Color(0xFF32747E)
                        ),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) 
                                        Icons.Filled.Visibility 
                                    else 
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) 
                                        "Hide password" 
                                    else 
                                        "Show password",
                                    tint = if (passwordVisible) Color(0xFF32747E) else Color.Gray
                                )
                            }
                        }
                    )

                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = Color(0xFF000000),
                            fontSize = 14.sp,
                            fontFamily = montserratFamily,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.25.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Handle sign in */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF32747E)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text(
                            text = "SIGN IN",
                            fontSize = 16.sp,
                            fontFamily = montserratFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    TextButton(
                        onClick = onSignUpClick,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Don't have an account? Sign up",
                            color = Color(0xFF000000),
                            fontSize = 14.sp,
                            fontFamily = montserratFamily,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.25.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

        }
    }
}