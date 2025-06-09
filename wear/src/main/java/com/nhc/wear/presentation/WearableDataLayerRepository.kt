package com.nhc.wear.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.nio.charset.StandardCharsets

// Singleton object to manage data layer communication and state
object WearableDataLayerRepository {

    private const val TAG = "WearableDataRepo"

    // StateFlow to hold the latest QR code data
    private val _qrCodeDataFlow = MutableStateFlow("No QR Code Scanned Yet")
    val qrCodeDataFlow: StateFlow<String> = _qrCodeDataFlow

    // Function to update the QR code data when received by the service
    fun updateQrCodeData(data: String) {
        _qrCodeDataFlow.value = data
        Log.d(TAG, "QR Code Data updated in repository: $data")
    }

    // Function to send start scanner message to phone
//    suspend fun sendStartScannerMessage(context: Context) {
//        val messageClient = Wearable.getMessageClient(context)
//        try {
//            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
//            if (nodes.isNotEmpty()) {
//                nodes.forEach { node ->
//                    messageClient.sendMessage(node.id, "/start-scanner", null)
//                        .addOnSuccessListener { Log.d(TAG, "Message sent to phone to start scanner") }
//                        .addOnFailureListener { e -> Log.e(TAG, "Failed to send message to phone", e) }
//                }
//            } else {
//                Log.w(TAG, "No connected Wear OS devices found.")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error getting connected nodes: ${e.message}")
//        }
//    }

    suspend fun sendStartScannerMessage(context: Context, targetPhoneAppId: String) {
        val messageClient = Wearable.getMessageClient(context)
        val payload = targetPhoneAppId.toByteArray(StandardCharsets.UTF_8)

        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isNotEmpty()) {
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, "/start-scanner", payload)
                        .addOnSuccessListener { Log.d(TAG, "Targeted message sent to phone to start scanner for ID: $targetPhoneAppId") }
                        .addOnFailureListener { e -> Log.e(TAG, "Failed to send targeted message to phone", e) }
                }
            } else {
                Log.w(TAG, "No connected Wear OS devices found.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting connected nodes: ${e.message}")
        }
    }
}