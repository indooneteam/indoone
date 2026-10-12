package com.indoone.connect.integrations.instagram

import androidx.compose.runtime.Composable
import com.indoone.connect.integrations.IntegrationPlatform
import com.indoone.connect.integrations.IntegrationSetupScaffold

@Composable
fun InstagramConnectScreen(
    onBack: () -> Unit,
) {
    IntegrationSetupScaffold(
        platform = IntegrationPlatform.INSTAGRAM,
        flowSteps = listOf(
            "Authorize the business owner’s Instagram professional account.",
            "Resolve the exact connected Instagram account on the backend.",
            "Load posts and Reels owned by that account.",
            "Bind an automation rule to one specific post or Reel.",
            "On a matching comment, send the stored public reply and/or DM to that same user.",
            "Record the inbound event and outbound action so duplicate webhooks cannot send duplicate replies.",
        ),
        platformNote = "V1 comment automation is deterministic: the backend reads the saved trigger and fixed reply text. AI is optional and is not required for a fixed comment-to-DM rule.",
        onBack = onBack,
    )
}
