// The two platforms Connect can set up, word for word from
// android/connect/integrations/IntegrationPlatform.kt, and the flow steps and
// platform note from WhatsAppConnectScreen.kt and InstagramConnectScreen.kt.

window.IndooneIntegrationPlatforms = {
  whatsapp: {
    key: 'whatsapp',
    title: 'WhatsApp',
    description:
      'Connect a WhatsApp Business account for customer messages and automated replies.',
    accountLabel: 'WhatsApp Business account',
    steps: [
      'Authorize the business owner\u2019s WhatsApp Business account.',
      'Resolve the exact connected business account and phone number on the backend.',
      'Receive customer messages through the WhatsApp webhook.',
      'Map each event to the correct Indoone connection before processing.',
      'Apply the saved automation or optional AI action for that connection.',
      'Send the reply through the same WhatsApp business account and record delivery status.'
    ],
    note:
      'The Android app does not receive provider webhooks directly. The backend owns webhook verification, account isolation, automation execution and outbound replies.'
  },

  instagram: {
    key: 'instagram',
    title: 'Instagram',
    description:
      'Connect a professional Instagram account for comments, DMs, posts and Reels automation.',
    accountLabel: 'Instagram professional account',
    steps: [
      'Authorize the business owner\u2019s Instagram professional account.',
      'Resolve the exact connected Instagram account on the backend.',
      'Load posts and Reels owned by that account.',
      'Bind an automation rule to one specific post or Reel.',
      'On a matching comment, send the stored public reply and/or DM to that same user.',
      'Record the inbound event and outbound action so duplicate webhooks cannot send duplicate replies.'
    ],
    note:
      'V1 comment automation is deterministic: the backend reads the saved trigger and fixed reply text. AI is optional and is not required for a fixed comment-to-DM rule.'
  }
};
