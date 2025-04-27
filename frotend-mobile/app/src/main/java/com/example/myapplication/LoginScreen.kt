package com.example.myapplication

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppTheme
import androidx.compose.ui.graphics.ColorFilter

@Composable
fun MainScreen(
    onMicrosoftLoginClick: () -> Unit = {}
) {
    // States for checkboxes
    var termsAgreed by remember { mutableStateOf(false) }
    var dataConsent by remember { mutableStateOf(false) }
    
    // For animated decorative elements
    val infiniteTransition = rememberInfiniteTransition(label = "circle_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha_animation"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9FAFB) // backgroundGray
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative elements
            Canvas(
                modifier = Modifier
                    .size(80.dp)
                    .offset((-40).dp, (-40).dp)
                    .alpha(alpha)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width / 2
                    )
                )
            }
            
            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Another decorative element inside the scrollable area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 32.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.BottomEnd)
                            .offset(32.dp, 32.dp)
                            .alpha(alpha)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF14B8A6), Color(0xFF0D9488)),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.width / 2
                            )
                        )
                    }
                    
                    // Main Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo and Header
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_with_no_text),
                                contentDescription = "Workforce Hub Logo",
                                modifier = Modifier.size(60.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "WORKFORCE HUB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color(0xFF1F2937)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "ENTERPRISE PORTAL",
                            fontSize = 14.sp,
                            letterSpacing = 2.sp,
                            color = Color(0xFF6B7280)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Auth Card
                        AuthCard(
                            termsAgreed = termsAgreed,
                            dataConsent = dataConsent,
                            onTermsAgreedChange = { termsAgreed = it },
                            onDataConsentChange = { dataConsent = it },
                            onMicrosoftLoginClick = onMicrosoftLoginClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthCard(
    termsAgreed: Boolean,
    dataConsent: Boolean,
    onTermsAgreedChange: (Boolean) -> Unit,
    onDataConsentChange: (Boolean) -> Unit,
    onMicrosoftLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Card Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF14B8A6))
                    )
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Enterprise Authentication",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        
        // Card Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Subtle background patterns
            Canvas(
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.TopEnd)
                    .offset(16.dp, (-16).dp)
                    .alpha(0.05f)
            ) {
                drawCircle(
                    color = Color(0xFF14B8A6),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                    radius = size.width / 2
                )
            }
            
            Canvas(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.BottomStart)
                    .offset((-16).dp, 16.dp)
                    .alpha(0.05f)
            ) {
                drawCircle(
                    color = Color(0xFF3B82F6),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                    radius = size.width / 2
                )
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Workforce Hub",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Access your enterprise dashboard securely with Microsoft",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Microsoft Sign In Card
                MicrosoftCard(onMicrosoftLoginClick)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Terms and Conditions
                TermsCard(
                    termsAgreed = termsAgreed,
                    dataConsent = dataConsent,
                    onTermsAgreedChange = onTermsAgreedChange,
                    onDataConsentChange = onDataConsentChange
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Benefits Section
                BenefitsCard()
            }
        }
        
        // Card Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF9FAFB), Color(0xFFE5E7EB))
                    )
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Contact your IT administrator for support",
                color = Color(0xFF6B7280),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MicrosoftCard(onMicrosoftLoginClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFF3F4F6),RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.microsoft_logo),
                        contentDescription = "Microsoft Logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "Microsoft Authentication",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    
                    Text(
                        text = "Secure single sign-on",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                
                Image(
                    painter = painterResource(id = R.drawable.shield_outline),
                    contentDescription = "Security Shield",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF3B82F6))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sign in with your organization's Microsoft account to access all enterprise resources securely.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Microsoft Login Button
            Button(
                onClick = onMicrosoftLoginClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0078D4)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.microsoft_logo),
                            contentDescription = "Microsoft Logo",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Continue with Microsoft",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        maxLines = 1
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF0078D4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "→",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TermsCard(
    termsAgreed: Boolean,
    dataConsent: Boolean,
    onTermsAgreedChange: (Boolean) -> Unit,
    onDataConsentChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Terms and Conditions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Terms Agreement Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAgreed,
                    onCheckedChange = onTermsAgreedChange
                )
                
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy",
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Data Consent Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = dataConsent,
                    onCheckedChange = onDataConsentChange
                )
                
                Text(
                    text = "I consent to the collection and processing of my personal information as described in the Privacy Policy",
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun BenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9FAFB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Enterprise Benefits:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Benefits with checkmarks
            BenefitItem(text = "Single sign-on across all enterprise applications")
            
            Spacer(modifier = Modifier.height(6.dp))
            
            BenefitItem(text = "Enhanced security with your organization's policies")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            BenefitItem(text = "Seamless access to all company resources")
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0xFF3B82F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFF3B82F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF4B5563),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainScreen()
    }
} 