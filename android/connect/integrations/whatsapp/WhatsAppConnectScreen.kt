package com.indoone.connect.integrations.whatsapp

import androidx.compose.runtime.Composable
import com.indoone.connect.integrations.IntegrationPlatform
import com.indoone.connect.integrations.IntegrationSetupScaffold

@Composable
fun WhatsAppConnectScreen(
    onBack: () -> Unit,
) {
    IntegrationSetupScaffold(
        platform = IntegrationPlatform.WHATSAPP,
        flowSteps = listOf(
            "Authorize the business owner’s WhatsApp Business account.",
            "Resolve the exact connected business account and phone number on the backend.",
            "Receive customer messages through the WhatsApp webhook.",
            "Map each event to the correct Indoone connection before processing.",
            "Apply the saved automation or optional AI action for that connection.",
            "Send the reply through the same WhatsApp business account and record delivery status.",
        ),
        platformNote = "The Android app does not receive provider webhooks directly. The backend owns webhook verification, account isolation, automation execution and outbound replies.",
        onBack = onBack,
    )
}
