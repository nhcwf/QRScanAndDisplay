package com.nhc.qrscananddisplay

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nhc.qrscananddisplay.ui.theme.QRScanAndDisplayTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

private const val TAG_ACTIVITY = "Scanner"
private const val TAG_SEND_DATA = "Scanner_SendData"
private const val TAG_QRCODE_ANALYZER = "Scanner_QrCodeAnalyzer"
private const val TAG_CAMERA_PREVIEW = "Scanner_CameraPreview"

class ScannerActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                hasCameraPermission.value = true
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
                finish() // Close the activity if permission is denied
            }
        }

    private var hasCameraPermission = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for permission and request if needed
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                hasCameraPermission.value = true
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        setContent {
            QRScanAndDisplayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (hasCameraPermission.value) {
                        ScannerScreen { scannedCode ->
                            // When a code is found, send it to the watch and finish this activity
                            Log.i(TAG_ACTIVITY, "scannedCode: $scannedCode")
                            sendDataToWatch(this, scannedCode)
                            finish()
                        }
                    }
                    // While waiting for permission result, a blank screen is shown, which is fine.
                }
            }
        }
    }
}

@Composable
fun ScannerScreen(onCodeScanned: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Point at QR Code",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        // CameraPreview takes up a large portion of the screen
        CameraPreview(onCodeScanned)
    }
}


// --- All the helper composables and functions now live here ---

@Composable
fun CameraPreview(onCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView: PreviewView? = null
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView?.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer(onCodeScanned))
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG_CAMERA_PREVIEW, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also {
                previewView = it
            }
        }, modifier = Modifier.fillMaxSize() // Make camera preview fill the screen
    )
}

class QrCodeAnalyzer(
    private val onCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private var isScanning = true

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val options =
                BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image).addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        isScanning = false // Stop further analysis to prevent multiple callbacks
                        barcodes.first().rawValue?.let {
                            onCodeScanned(it)
                        }
                    }
                }.addOnFailureListener {
                    Log.e(TAG_QRCODE_ANALYZER, "Failed to process image", it)
                }.addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}

fun sendDataToWatch(context: Context, data: String) {
    Log.d(TAG_SEND_DATA, "Attempting to send: $data")
    Toast.makeText(context, "Code Scanned. Sending to watch...", Toast.LENGTH_SHORT).show()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
            if (nodes.isEmpty()) {
                Log.e(TAG_SEND_DATA, "No watch connected.")
                return@launch
            }
            nodes.forEach { node ->
                Log.d(TAG_SEND_DATA, "Sending to node: ${node.displayName}")
                val path = "/invite-code"
                val payload = data.toByteArray(StandardCharsets.UTF_8)

                Wearable.getMessageClient(context).sendMessage(node.id, path, payload)
                    .addOnSuccessListener { Log.d(TAG_SEND_DATA, "Message sent successfully") }
                    .addOnFailureListener { e -> Log.e(TAG_SEND_DATA, "Message sending failed", e) }
            }
        } catch (e: Exception) {
            Log.e(TAG_SEND_DATA, "Task to get nodes failed", e)
        }
    }
}