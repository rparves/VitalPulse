@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vitapulse.app.food.*
import com.vitapulse.app.ui.AppViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScanScreen(vm: AppViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasCam by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCam = it }
    LaunchedEffect(Unit) { if (!hasCam) camLauncher.launch(Manifest.permission.CAMERA) }

    var mode by remember { mutableStateOf(ScanMode.PHOTO) }
    var liveLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var candidates by remember { mutableStateOf<List<FoodCandidate>?>(null) }
    var selected by remember { mutableStateOf<FoodCandidate?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    val analyzer = remember { FoodAnalyzer() }
    val paused = busy || candidates != null || selected != null || showSearch
    LaunchedEffect(mode) { analyzer.mode = mode; liveLabels = emptyList(); status = null }
    LaunchedEffect(paused) { analyzer.paused = paused }
    DisposableEffect(analyzer) {
        analyzer.onLabels = { liveLabels = it }
        analyzer.onBarcode = { code ->
            if (!busy) {
                busy = true
                analyzer.paused = true
                status = "Looking up $code…"
                scope.launch {
                    val found = OpenFoodFacts.byBarcode(code)
                    busy = false
                    if (found != null) { status = null; selected = found }
                    else status = "Product $code isn't in Open Food Facts yet. Try photo mode or search."
                }
            }
        }
        onDispose { analyzer.close() }
    }

    val identify: () -> Unit = {
        val labels = liveLabels
        if (labels.isEmpty()) {
            status = "Point the camera at your food and hold steady."
        } else {
            candidates = FoodCatalog.match(labels)
            val query = labels.firstOrNull { it.lowercase() !in FoodCatalog.GENERIC }
            if (query != null) {
                busy = true
                scope.launch {
                    val remote = OpenFoodFacts.search(query).take(5)
                    candidates = (candidates ?: emptyList()) + remote
                    busy = false
                }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCam) CameraPreview(analyzer, Modifier.fillMaxSize())
        else Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Camera permission is needed to scan food.", color = Color.White)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { camLauncher.launch(Manifest.permission.CAMERA) }) { Text("Allow camera") }
            TextButton(onClick = { showSearch = true }) { Text("Search instead") }
        }

        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close") }
            Spacer(Modifier.weight(1f))
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(mode == ScanMode.PHOTO, { mode = ScanMode.PHOTO }, SegmentedButtonDefaults.itemShape(0, 2)) {
                    Text("Food photo")
                }
                SegmentedButton(mode == ScanMode.BARCODE, { mode = ScanMode.BARCODE }, SegmentedButtonDefaults.itemShape(1, 2)) {
                    Text("Barcode")
                }
            }
        }

        // Bottom panel
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (mode == ScanMode.PHOTO && liveLabels.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    liveLabels.take(5).forEach { AssistChip(onClick = {}, label = { Text(it, color = Color.White) }) }
                }
            }
            Text(
                status ?: if (mode == ScanMode.PHOTO) "Frame your plate, then tap Identify"
                          else "Align the product barcode inside the frame",
                color = Color.White, style = MaterialTheme.typography.bodyMedium
            )
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (mode == ScanMode.PHOTO) {
                    Button(onClick = identify, enabled = !busy && hasCam) {
                        Icon(Icons.Rounded.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Identify")
                    }
                }
                OutlinedButton(onClick = { showSearch = true }) {
                    Icon(Icons.Rounded.Search, null, tint = Color.White); Spacer(Modifier.width(8.dp))
                    Text("Search", color = Color.White)
                }
            }
        }
    }

    candidates?.let { list ->
        ModalBottomSheet(onDismissRequest = { candidates = null }) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text("Is it one of these?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 8.dp))
                if (list.isEmpty() && !busy) {
                    Text("No confident match. Search by name instead.", Modifier.padding(vertical = 12.dp))
                }
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(list) { c -> CandidateRow(c) { selected = c } }
                }
                TextButton(onClick = { candidates = null; showSearch = true }) { Text("Not here? Search by name") }
            }
        }
    }
    if (showSearch) FoodSearchDialog(onPick = { selected = it; showSearch = false }, onDismiss = { showSearch = false })
    selected?.let { c ->
        AddFoodDialog(c, onDismiss = { selected = null }) { g, meal ->
            vm.logFood(c, g, meal)
            Toast.makeText(context, "Logged ${c.name.take(40)}", Toast.LENGTH_SHORT).show()
            selected = null
            candidates = null
            onClose()
        }
    }
}

@Composable
private fun CameraPreview(analyzer: FoodAnalyzer, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val executor = Executors.newSingleThreadExecutor()
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }
            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching { providerFuture.get().unbindAll() }
            executor.shutdown()
        }
    }
    AndroidView({ previewView }, modifier)
}
