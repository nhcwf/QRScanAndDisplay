package com.nhc.wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.nhc.wear.presentation.theme.QRScanAndDisplayTheme
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    var qrCodeData by mutableStateOf("No QR Code Scanned Yet")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(qrCodeData = qrCodeData)
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
        Log.d(TAG_WEAR_ACTIVITY, "MessageClient listener added")
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
        Log.d(TAG_WEAR_ACTIVITY, "MessageClient listener removed")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG_WEAR_ACTIVITY, "Message received: ${messageEvent.path}")
        if (messageEvent.path == "/invite-code") {
            val data = String(messageEvent.data, StandardCharsets.UTF_8)
            Log.d(TAG_WEAR_ACTIVITY, "Received invite code: $data")

            // Update the UI on the main thread
            lifecycleScope.launch {
                qrCodeData = "Received Code:\n$data"
            }
        }
    }

    companion object {
        private const val TAG_WEAR_ACTIVITY = "WearMainActivity"
    }
}

@Composable
fun WearApp(qrCodeData: String) {
    QRScanAndDisplayTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(qrCodeData = qrCodeData) // Pass qrCodeData to Greeting
        }
    }
}

@Composable
fun Greeting(qrCodeData: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = qrCodeData // Display the received QR code data
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(qrCodeData = "Preview QR Code")
}