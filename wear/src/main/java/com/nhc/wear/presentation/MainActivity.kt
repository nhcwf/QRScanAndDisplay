package com.nhc.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.gms.wearable.MessageClient
import com.nhc.wear.presentation.theme.QRScanAndDisplayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        val data = intent.getStringExtra(MessageListenerService.EXTRA_CODE) ?: ""

        setContent {
            WearApp(data)
        }
    }
}

@Composable
fun WearApp(inviteCode: String) {
    QRScanAndDisplayTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            InviteCode(inviteCode)
        }
    }
}

@Composable
fun InviteCode(code: String) {
    if (code.isEmpty()) {
        Text(
            "Invite Code not received or is empty.",
            textAlign = TextAlign.Center
        )
    } else {
        Text("Invite Code: $code")
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("")
}