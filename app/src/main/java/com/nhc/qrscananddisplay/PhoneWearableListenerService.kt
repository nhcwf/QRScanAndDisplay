package com.nhc.qrscananddisplay

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.charset.StandardCharsets

class PhoneWearableListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "PhoneWearableService"
    }

//    override fun onMessageReceived(messageEvent: MessageEvent) {
//        super.onMessageReceived(messageEvent)
//        Log.d(TAG, "Message received: ${messageEvent.path} from ${messageEvent.sourceNodeId}")
//
//        if (messageEvent.path == "/start-scanner") {
//            Log.d(TAG, "Received request to start scanner from watch.")
//
//            val launchScannerIntent = Intent(this, ScannerActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            startActivity(launchScannerIntent)
//        }
//    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Message received: ${messageEvent.path} from ${messageEvent.sourceNodeId}")

        // Check if the message is the targeted scanner request
        if (messageEvent.path == "/start-scanner") {
            val targetAppIdPayload = String(messageEvent.data, StandardCharsets.UTF_8)
            val currentAppId = "com.nhc.qrscananddisplay"

            Log.d(TAG, "Received targeted scanner request for ID: $targetAppIdPayload. Current app ID: $currentAppId")

            if (targetAppIdPayload == currentAppId) {
                Log.d(TAG, "Target ID matches current app ID. Launching ScannerActivity.")
                val launchScannerIntent = Intent(this, ScannerActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(launchScannerIntent)
            } else {
                Log.d(TAG, "Target ID does NOT match current app ID. Ignoring message.")
            }
        }
        // Add other message handlers here if needed
        // e.g., if (messageEvent.getPath() == "/some-other-general-message") { ... }
    }
}