package com.vitapulse.app.food

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

enum class ScanMode { PHOTO, BARCODE }

/** Runs entirely on-device: frames never leave the phone. */
class FoodAnalyzer : ImageAnalysis.Analyzer {
    @Volatile var mode: ScanMode = ScanMode.PHOTO
    @Volatile var paused: Boolean = false
    var onLabels: (List<String>) -> Unit = {}
    var onBarcode: (String) -> Unit = {}

    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build()
    )
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder().setConfidenceThreshold(0.55f).build()
    )
    private var lastLabelAt = 0L
    private var lastCode: String? = null
    private var lastCodeAt = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(proxy: ImageProxy) {
        val media = proxy.image
        if (paused || media == null) { proxy.close(); return }
        val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
        when (mode) {
            ScanMode.BARCODE -> barcodeScanner.process(image)
                .addOnSuccessListener { codes ->
                    val code = codes.firstNotNullOfOrNull { it.rawValue } ?: return@addOnSuccessListener
                    val now = SystemClock.elapsedRealtime()
                    if (code == lastCode && now - lastCodeAt < 6000) return@addOnSuccessListener
                    lastCode = code; lastCodeAt = now
                    if (!paused) onBarcode(code)
                }
                .addOnCompleteListener { proxy.close() }

            ScanMode.PHOTO -> {
                val now = SystemClock.elapsedRealtime()
                if (now - lastLabelAt < 600) { proxy.close(); return }
                lastLabelAt = now
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        onLabels(labels.sortedByDescending { it.confidence }.map { it.text })
                    }
                    .addOnCompleteListener { proxy.close() }
            }
        }
    }

    fun close() {
        barcodeScanner.close()
        labeler.close()
    }
}
