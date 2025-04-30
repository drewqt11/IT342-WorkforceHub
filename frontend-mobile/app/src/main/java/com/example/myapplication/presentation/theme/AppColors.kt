package com.example.myapplication.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Enhanced color scheme for the application
 */
object AppColors {
    // Primary blues
    val blue50 = Color(0xFFEFF6FF)
    val blue100 = Color(0xFFDCEAFF)
    val blue300 = Color(0xFF93C5FD)
    val blue500 = Color(0xFF3B82F6)
    val blue700 = Color(0xFF2563EB)
    val blue900 = Color(0xFF1E3A8A)
    
    // Secondary teals
    val teal50 = Color(0xFFF0FDFA)
    val teal100 = Color(0xFFCCFBF1)
    val teal300 = Color(0xFF5EEAD4)
    val teal500 = Color(0xFF14B8A6)
    val teal700 = Color(0xFF0D9488)
    val teal900 = Color(0xFF134E4A)
    
    // Neutrals
    val white = Color(0xFFFFFFFF)
    val gray50 = Color(0xFFF9FAFB)
    val gray100 = Color(0xFFF3F4F6)
    val gray200 = Color(0xFFE5E7EB)
    val gray300 = Color(0xFFD1D5DB)
    val gray500 = Color(0xFF6B7280)
    val gray700 = Color(0xFF374151)
    val gray800 = Color(0xFF1F2937)
    
    // Status colors
    val green = Color(0xFF22C55E)
    val greenLight = Color(0xFFDCFCE7)
    val amber = Color(0xFFF59E0B)
    val amberLight = Color(0xFFFEF3C7)
    val red = Color(0xFFEF4444)
    val redLight = Color(0xFFFEE2E2)
    
    // Gradients
    val blueGradient = Brush.verticalGradient(
        colors = listOf(blue700, blue500)
    )
    
    val tealGradient = Brush.verticalGradient(
        colors = listOf(teal700, teal500)
    )
} 