package cit.edu.workforcehub.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cit.edu.workforcehub.presentation.theme.AppColors
import cit.edu.workforcehub.presentation.theme.AppTheme

/**
 * Notification type enum to determine the style and icon of the notification
 */
enum class NotificationType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

/**
 * A dialog that displays a notification with an icon, title, message, and action button.
 *
 * @param title The title of the notification
 * @param message The detailed message to display
 * @param type The type of notification to display (affects colors and icon)
 * @param onDismiss Callback when the dialog is dismissed
 * @param buttonText Text for the action button (default: "OK")
 * @param properties Dialog properties for customizing behavior
 */
@Composable
fun NotificationDialog(
    title: String,
    message: String,
    type: NotificationType = NotificationType.INFO,
    onDismiss: () -> Unit,
    buttonText: String = "OK",
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true
    )
) {
    // Determine button color based on notification type
    val buttonColor = when (type) {
        NotificationType.INFO -> Color(0xFF2563EB) // Blue
        NotificationType.WARNING -> Color(0xFF2563EB) // Blue
        NotificationType.ERROR -> Color(0xFF2563EB) // Blue
        NotificationType.SUCCESS -> Color(0xFF2563EB) // Blue
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Message
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WarningNotificationDialogPreview() {
    AppTheme {
        Surface {
            NotificationDialog(
                title = "Already Clocked In",
                message = "You have already clocked in today. Please clock out first before clocking in again.",
                type = NotificationType.WARNING,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorNotificationDialogPreview() {
    AppTheme {
        Surface {
            NotificationDialog(
                title = "Connection Error",
                message = "Failed to connect to the server. Please check your connection and try again.",
                type = NotificationType.ERROR,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuccessNotificationDialogPreview() {
    AppTheme {
        Surface {
            NotificationDialog(
                title = "Success",
                message = "You have successfully clocked in for today.",
                type = NotificationType.SUCCESS,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoNotificationDialogPreview() {
    AppTheme {
        Surface {
            NotificationDialog(
                title = "Information",
                message = "Your schedule has been updated for next week.",
                type = NotificationType.INFO,
                onDismiss = {}
            )
        }
    }
} 