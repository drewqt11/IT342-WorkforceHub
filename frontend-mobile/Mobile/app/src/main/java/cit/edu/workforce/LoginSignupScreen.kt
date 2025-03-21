package cit.edu.workforce

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import cit.edu.workforce.R

private val montserratFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_black, FontWeight.Black)
)

@Composable
fun AnimatedButton(
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

@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale),
        interactionSource = interactionSource
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun LoginSignupScreen(
    isLogin: Boolean,
    onNavigateToSignup: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_nowhitebg),
                contentDescription = "WF Club Logo",
                modifier = Modifier
                    .size(230.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Your complete HR management solution",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Sign In Button
            AnimatedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                containerColor = Color(0xFF32747E),
                text = "SIGN IN"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Button
            AnimatedButton(
                onClick = onNavigateToSignup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF000000),
                text = "SIGN UP"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Social Media Login Text
            Text(
                text = "Or continue with",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.25.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Social Media Icons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Icon
                AnimatedIconButton(
                    onClick = { /* Handle Google login */ },
                    icon = R.drawable.ic_google,
                    contentDescription = "Login with Google",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Facebook Icon
                AnimatedIconButton(
                    onClick = { /* Handle Facebook login */ },
                    icon = R.drawable.ic_facebook,
                    contentDescription = "Login with Facebook",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                // GitHub Icon
                AnimatedIconButton(
                    onClick = { /* Handle GitHub login */ },
                    icon = R.drawable.ic_github,
                    contentDescription = "Login with GitHub",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
} 