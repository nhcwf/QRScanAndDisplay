package com.nhc.qrscananddisplay

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneCommunicationManager(private val context: Context) {

    companion object {
        private const val TAG = "PhoneCommManager"
        private const val INVITE_CODE_PATH = "/invite-code"
    }

    fun sendQrCodeToWatch(data: String) {
        Log.d(TAG, "Attempting to send QR code: $data")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
                if (nodes.isEmpty()) {
                    Log.e(TAG, "No watch connected.")
                    return@launch
                }
                nodes.forEach { node ->
                    Log.d(TAG, "Sending QR code to node: ${node.displayName}")
                    val payload = data.toByteArray(StandardCharsets.UTF_8)

                    Wearable.getMessageClient(context).sendMessage(node.id, INVITE_CODE_PATH, payload)
                        .addOnSuccessListener { Log.d(TAG, "QR code message sent successfully") }
                        .addOnFailureListener { e -> Log.e(TAG, "QR code message sending failed", e) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Task to get nodes failed while sending QR code", e)
            }
        }
    }
}