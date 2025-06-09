package com.nhc.wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.nhc.wear.presentation.theme.QRScanAndDisplayTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG_WEAR_ACTIVITY = "WearMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            // Collect the QR code data from the repository's StateFlow
            val qrCodeData by WearableDataLayerRepository.qrCodeDataFlow.collectAsState()

            WearApp(qrCodeData = qrCodeData) {
                // Trigger sending message via the repository
                lifecycleScope.launch {
                    WearableDataLayerRepository.sendStartScannerMessage(applicationContext, "com.nhc.qrscananddisplay")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // No need to register BroadcastReceiver, data is collected via StateFlow
        Log.d(TAG_WEAR_ACTIVITY, "MainActivity resumed, collecting from StateFlow")
    }

    override fun onPause() {
        super.onPause()
        // No need to unregister BroadcastReceiver
        Log.d(TAG_WEAR_ACTIVITY, "MainActivity paused")
    }
}

@Composable
fun WearApp(qrCodeData: String, onScanFromPhoneClick: () -> Unit) {
    QRScanAndDisplayTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = qrCodeData
                )

                Spacer(Modifier.height(8.dp))

                Button(onClick = onScanFromPhoneClick) {
                    Text("Scan from Phone")
                }
            }
        }
    }
}