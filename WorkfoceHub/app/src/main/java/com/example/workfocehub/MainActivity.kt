package com.example.workfocehub

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var microsoftLoginButton: MaterialButton
    private lateinit var termsAgreeCheckbox: CheckBox
    private lateinit var dataConsentCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        microsoftLoginButton = findViewById(R.id.microsoftLoginButton)
        termsAgreeCheckbox = findViewById(R.id.termsAgreeCheckbox)
        dataConsentCheckbox = findViewById(R.id.dataConsentCheckbox)

        // Set up button click listener
        microsoftLoginButton.setOnClickListener {
            handleMicrosoftLogin()
        }

        // Apply different configurations based on screen size and orientation
        applyResponsiveConfiguration()
    }

    private fun handleMicrosoftLogin() {
        // Validate terms acceptance
        if (!termsAgreeCheckbox.isChecked || !dataConsentCheckbox.isChecked) {
            Toast.makeText(
                this,
                "Please accept the terms and conditions to continue",
                Toast.LENGTH_SHORT
            ).show()

            // Shake animation for checkboxes that aren't checked
            if (!termsAgreeCheckbox.isChecked) {
                shakeView(termsAgreeCheckbox)
            }

            if (!dataConsentCheckbox.isChecked) {
                shakeView(dataConsentCheckbox)
            }

            return
        }

        // Show loading state
        microsoftLoginButton.isEnabled = false
        microsoftLoginButton.text = "Authenticating..."

        try {
            // Apply button glow animation
            val buttonGlowAnimation = AnimatorInflater.loadAnimator(
                this,
                R.animator.button_glow_animation
            ) as AnimatorSet
            buttonGlowAnimation.setTarget(microsoftLoginButton)
            buttonGlowAnimation.start()
        } catch (e: Exception) {
            // More detailed error logging
            Log.e("MainActivity", "Error animating button: ${e.message}", e)

            // Fallback animation if the XML animation fails
            microsoftLoginButton.animate()
                .alpha(0.7f)
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(300)
                .withEndAction {
                    microsoftLoginButton.animate()
                        .alpha(1.0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(300)
                        .start()
                }
                .start()
        }

        // Simulate authentication process
        microsoftLoginButton.postDelayed({
            // Authentication successful
            Toast.makeText(
                this,
                "Authentication successful!",
                Toast.LENGTH_SHORT
            ).show()

            // Reset button state
            microsoftLoginButton.isEnabled = true
            microsoftLoginButton.text = "Continue with Microsoft"

        }, 1500) // Simulate network delay
    }


    private fun setupMicrosoftButton() {
        val microsoftLoginButton = findViewById<MaterialButton>(R.id.microsoftLoginButton)
        val arrowRightIcon = findViewById<ImageView>(R.id.arrowRightIcon)

        // Calculate proper padding based on screen width
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Adjust button text alignment and padding
        microsoftLoginButton.post {
            val buttonWidth = microsoftLoginButton.width
            val iconWidth = resources.getDimensionPixelSize(R.dimen.icon_size) // Assuming you have this dimension
            val arrowWidth = arrowRightIcon.layoutParams.width
            val textWidth = microsoftLoginButton.paint.measureText(microsoftLoginButton.text.toString())

            // Calculate total content width (icon + padding + text + padding + arrow)
            val contentWidth = iconWidth + 12 + textWidth + 12 + arrowWidth

            // Calculate extra padding needed to center the content
            val extraPadding = (buttonWidth - contentWidth) / 2

            // Apply padding, but ensure it's not negative
            val startPadding = Math.max(extraPadding.toInt(), 16)

            microsoftLoginButton.setPadding(startPadding, microsoftLoginButton.paddingTop,
                startPadding, microsoftLoginButton.paddingBottom)
        }

// Adjust for different screen sizes
        if (screenWidth <= 360) { // Small screens
            // Handle small screen adjustments
        }

        // Adjust for different screen sizes
        if (screenWidth <= 360) { // Small screens
            microsoftLoginButton.textSize = 14f
        } else if (screenWidth >= 720) { // Large screens
            microsoftLoginButton.textSize = 16f
        }
    }

    private fun shakeView(view: View) {
        try {
            val shakeAnimation = AnimatorInflater.loadAnimator(
                this,
                R.animator.shake_animation
            ) as AnimatorSet
            shakeAnimation.setTarget(view)
            shakeAnimation.start()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error shaking view: ${e.message}")
        }
    }

    private fun applyResponsiveConfiguration() {
        // Get current configuration
        val configuration = resources.configuration

        // Check if we're on a tablet (large screen)
        val isTablet = configuration.screenWidthDp >= 600

        // Check if we're in landscape mode
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Apply specific adjustments based on device and orientation
        if (isTablet) {
            // For tablets, we can make some elements larger
            microsoftLoginButton.textSize = resources.getDimension(R.dimen.button_text_size)

            // On tablets in landscape, we might want to adjust the layout further
            if (isLandscape) {
                // Example: adjust padding or margins for landscape tablet
                findViewById<View>(R.id.mainContentContainer)?.apply {
                    val params = layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    params.matchConstraintMaxWidth = resources.getDimensionPixelSize(R.dimen.max_width_tablet_landscape)
                    layoutParams = params
                }
            }
        } else {
            // For phones in landscape, we might want to adjust some elements
            if (isLandscape) {
                // Example: make some elements more compact in phone landscape
                findViewById<View>(R.id.logoContainer)?.visibility = View.GONE
            }
        }
    }

    // Handle configuration changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyResponsiveConfiguration()
    }
}