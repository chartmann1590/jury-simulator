package com.charles.jurysim.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.charles.jurysim.data.model.Message

/** Developer contact, kept in sync with play-store/contact-email.txt. */
private const val DEVELOPER_EMAIL = "charles.h.hartmann1@gmail.com"

/**
 * Confirmation dialog for the long-press "Report this content" action on
 * AI-generated chat messages.
 *
 * Confirming does two things:
 * 1. [onReport] records the flag locally on the device (DataStore).
 * 2. Opens a pre-filled email to the developer so the user can choose
 *    to send a copy — the app itself transmits nothing.
 */
@Composable
fun ReportContentDialog(
    message: Message,
    onDismiss: () -> Unit,
    onReport: (Message) -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report this content") },
        text = {
            Text(
                "Report this AI-generated message? The report is saved on " +
                    "this device, and you can also send a copy to the " +
                    "developer by email."
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onReport(message)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$DEVELOPER_EMAIL")
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Jury Simulator — AI content report"
                        )
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Speaker: ${message.speaker ?: "AI"}\n\n" +
                                "Reported message:\n${message.content}"
                        )
                    }
                    runCatching { context.startActivity(intent) }
                    onDismiss()
                }
            ) {
                Text("Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
