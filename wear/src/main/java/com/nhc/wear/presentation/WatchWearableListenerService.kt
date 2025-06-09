package com.nhc.wear.presentation

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.charset.StandardCharsets

class WatchWearableListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WatchWearableService"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Message received: ${messageEvent.path} from ${messageEvent.sourceNodeId}")

        if (messageEvent.path == "/invite-code") {
            val data = String(messageEvent.data, StandardCharsets.UTF_8)
            Log.d(TAG, "Received invite code: $data")

            WearableDataLayerRepository.updateQrCodeData(data)
        }
    }
}