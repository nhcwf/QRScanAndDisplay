package com.nhc.qrscananddisplay

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.wearable.Wearable
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nhc.qrscananddisplay.ui.theme.QRScanAndDisplayTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    // Handles the result of the permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted. We can now start the camera.
                // This state change will trigger a recomposition and show the camera preview.
                hasCameraPermission.value = true
            } else {
                // Explain to the user that the feature is unavailable
                Log.e("MainActivity", "Camera permission denied")
            }
        }

    private var hasCameraPermission = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for permission right away
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission.value = true
        }

        setContent {
            QRScanAndDisplayTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(
                        hasPermission = hasCameraPermission.value,
                        onRequestPermission = {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(hasPermission: Boolean, onRequestPermission: () -> Unit) {
    var scannedCode by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasPermission) {
            Text("Scanning...", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            // Camera Preview Composable
            CameraPreview { qrCodeValue ->
                scannedCode = qrCodeValue // Update the state with the scanned code
                // --- THIS IS WHERE WE SEND DATA TO THE WATCH ---
                sendDataToWatch(context, qrCodeValue)
            }

        } else {
            // Show a button to request permission
            Button(onClick = onRequestPermission) {
                Text("Request Camera Permission")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Display the last scanned code
        if (scannedCode != null) {
            Text(
                text = "Last Scanned Code:",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = scannedCode!!,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}


@Composable
fun CameraPreview(onCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView: PreviewView? = null

    // Using a disposable effect to manage the camera lifecycle
    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraExecutor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView?.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Setup the image analyzer for barcode scanning
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer(onCodeScanned))
                }

            try {
                cameraProvider.unbindAll() // Unbind use cases before rebinding
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            // Shut down the camera executor when the composable is disposed
            cameraExecutor.shutdown()
        }
    }

    // This is the actual view that displays the camera feed
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also {
                previewView = it
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

// The heart of the QR code analysis
class QrCodeAnalyzer(
    private val onCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        rawValue?.let {
                            // We found a QR code! Call the callback.
                            onCodeScanned(it)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QrCodeAnalyzer", "Failed to process image", it)
                }
                .addOnCompleteListener {
                    // It's crucial to close the imageProxy
                    imageProxy.close()
                }
        }
    }
}

// --- Placeholder for the communication logic ---
fun sendDataToWatch(context: android.content.Context, data: String) {
    Log.d("SendData", "Attempting to send: $data")
    // We will implement this in the next step
}