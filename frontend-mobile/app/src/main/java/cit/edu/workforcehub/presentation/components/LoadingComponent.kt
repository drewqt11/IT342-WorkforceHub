package cit.edu.workforcehub.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch

/**
 * A loading component that shows a centered circular progress indicator
 * with subtle animation effects in the background.
 * 
 * @param modifier Modifier for the component
 */
@Composable
fun LoadingComponent(
    modifier: Modifier = Modifier
) {
    // Create scale animation
    val scaleAnimation = remember { Animatable(0.8f) }
    // Create opacity animation
    val alphaAnimation = remember { Animatable(0.5f) }
    
    // Start the animations
    LaunchedEffect(Unit) {
        // Launch pulse animation
        launch {
            scaleAnimation.animateTo(
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        // Launch fade animation
        launch {
            alphaAnimation.animateTo(
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing circle (subtle glow)
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scaleAnimation.value)
                .alpha(alphaAnimation.value)
                .clip(CircleShape)
                .background(AppColors.blue100.copy(alpha = 0.3f))
                .align(Alignment.Center)
        )
        
        // Middle static circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AppColors.blue50.copy(alpha = 0.6f))
                .align(Alignment.Center)
        )
        
        // Main progress indicator
        CircularProgressIndicator(
            color = AppColors.blue500,
            modifier = Modifier.size(50.dp),
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Preview of the LoadingComponent
 */
@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun LoadingComponentPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray50
    ) {
        LoadingComponent()
    }
} 