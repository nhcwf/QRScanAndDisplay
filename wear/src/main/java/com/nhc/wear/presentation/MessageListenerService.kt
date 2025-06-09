package com.nhc.wear.presentation

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.charset.StandardCharsets

class MessageListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("WearableListenerService", "WearableListenerService is starting...")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("MessageListenerService", "New message received: ${messageEvent.path}")

        if (messageEvent.path == INVITE_CODE_PATH) {

            val receivedData = String(messageEvent.data, StandardCharsets.UTF_8)
            Log.d("MessageListenerService", "Invite Code Received: $receivedData")

            val intent = Intent(ACTION_CODE_RECEIVED).apply {
                putExtra(EXTRA_CODE, receivedData)
            }

            startActivity(intent)
        }
    }

    companion object {
        const val INVITE_CODE_PATH = "/invite-code"
        const val ACTION_CODE_RECEIVED = "com.nhc.qrscananddisplay.CODE_RECEIVED"
        const val EXTRA_CODE = "extra_code"
    }
}