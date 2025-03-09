package com.example.loginpage

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.benchmark.perfetto.ExperimentalPerfettoTraceProcessorApi
import androidx.benchmark.perfetto.Row
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Scanner
import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val isScanning = remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                startCamera(cameraProviderFuture, lifecycleOwner, previewView, context, cameraExecutor, isScanning, navController)
            } else {
                // Handle permission denial
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(cameraProviderFuture, lifecycleOwner, previewView, context, cameraExecutor, isScanning, navController)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cashierName = navController.currentBackStackEntry?.arguments?.getString("cashierName")
    Log.d("ScannerScreen", "Cashier Name: $cashierName")

    BackHandler {
        navController.navigate("Routes.LoginScreen") {
            popUpTo("Routes.LoginScreen") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                cashierName = cashierName
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF5C4033))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cashier: ${cashierName ?: "No User"}",
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "Branch: Dapitan",
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Live Camera Feed
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // Scan Button
            Button(
                onClick = { isScanning.value = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF008000),
                    contentColor = Color(0xFFFFFFFF)
                ),
                modifier = Modifier
                    .height(64.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp)
                    .bounceClick()
            ) {
                Text(text = "SCAN ID", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Food Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0C1A6))
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    items(
                        listOf(
                            Pair("Water", R.drawable.drinks),
                            Pair("Pasta", R.drawable.pasta),
                            Pair("Snacks", R.drawable.snacks)
                        )
                    ) { (description, drawableId) ->
                        val isSelected = remember { mutableStateOf(false) }
                        val alphaValue by animateFloatAsState(if (isSelected.value) 0.5f else 1f)

                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                                .padding(horizontal = 8.dp)
                                .clickable { isSelected.value = !isSelected.value }
                                .border(
                                    width = if (isSelected.value) 2.dp else 0.dp,
                                    color = if (isSelected.value) Color(0xFF008000) else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = drawableId),
                                contentDescription = description,
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(alphaValue)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun startCamera(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    context: android.content.Context,
    cameraExecutor: ExecutorService,
    isScanning: MutableState<Boolean>,
    navController: NavController
) {
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
            if (isScanning.value) {
                processImageForTextRecognition(imageProxy, context, isScanning, navController)
            } else {
                imageProxy.close()
            }
        })

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageForTextRecognition(
    imageProxy: ImageProxy,
    context: android.content.Context,
    isScanning: MutableState<Boolean>,
    navController: NavController
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage).addOnSuccessListener { visionText ->
            processText(visionText, context, navController)
        }.addOnFailureListener { e ->
            Log.e("TextRecognition", "Failed to process image", e)
        }.addOnCompleteListener {
            imageProxy.close()
            isScanning.value = false // Reset scanning state after processing
        }
    } else {
        imageProxy.close()
        isScanning.value = false // Reset scanning state if no media image
    }
}


private fun processText(visionText: Text, context: android.content.Context, navController: NavController) {
    if (!detected(visionText)) {
        Log.d("ProcessText", "Not a PWD or Senior Citizen ID. Skipping extraction.")
        return
    }

    val fullText = visionText.textBlocks.joinToString("\n") { it.text }
    val name = extractName(fullText)
    val idNumber = extractIdNumber(fullText)
    val city = extractCity(fullText)

    Log.d("ProcessText", "Extracted Name: $name")
    Log.d("ProcessText", "Extracted ID Number: $idNumber")
    Log.d("ProcessText", "Extracted City: $city")

    // Navigate to ConfirmationScreen with the extracted data
    navController.navigate("confirmation_screen/$name/$idNumber/$city")
}

private fun detected(visionText: Text): Boolean {
    val pwdKeywords = listOf(
        "PWD", "PERSONS WITH DISABILITY", "DISABILITY", "PWD ID", "PWD IDENTIFICATION",
        "PHILHEALTH", "DISABILITY ID", "DISABILITY CARD", "PSYCHOSOCIAL", "MENTAL", "PHYSICAL", "VISUAL",
        "HEARING", "LEARNING", "SPEECH", "ORTHOPEDIC", "VISION", "IMPAIRMENT"
    )
    val seniorCitizenKeywords = listOf(
        "SENIOR CITIZEN", "OSCA", "SENIOR CITIZEN ID", "OSCA ID", "SENIOR CITIZEN CARD",
        "OFFICE OF THE SENIOR CITIZENS AFFAIRS", "OSCA IDENTIFICATION"
    )
    val keywords = pwdKeywords + seniorCitizenKeywords
    val text = visionText.text
    return keywords.any { keyword -> text.contains(keyword, ignoreCase = true) }
}

private fun extractName(text: String): String {
    val lines = text.split("\n")
    for (i in lines.indices) {
        if (lines[i].contains("NAME", ignoreCase = true)) {
            val sameLineName = extractNameFromSameLine(lines[i])
            if (sameLineName.isNotEmpty()) {
                return sameLineName
            }
            if (i > 0 && isValidName(lines[i - 1])) {
                return lines[i - 1].trim()
            }
            if (i < lines.size - 1 && isValidName(lines[i + 1])) {
                return lines[i + 1].trim()
            }
        }
    }
    return ""
}

private fun extractNameFromSameLine(line: String): String {
    val nameRegex = Regex("""NAME:\s*([A-Za-zñÑ.,\-']+(?:\s+[A-Za-zñÑ.,\-']+)*)""", RegexOption.IGNORE_CASE)
    return nameRegex.find(line)?.groupValues?.get(1)?.trim() ?: ""
}

private fun isValidName(line: String): Boolean {
    val nameRegex = Regex("""^[A-Za-zñÑ.,\-']+(?:\s+[A-Za-zñÑ.,\-']+)+$""")
    val invalidKeywords = listOf(
        "Address", "Date", "ID", "No", "Signature", "Birthday", "Age", "Gender", "Blood",
        "MUNICIPAL", "Municipality", "Barangay", "Pangalan", "Lungsod", "City", "Government",
        "Residence", "WELFARE", "OFFICE", "SENIOR", "CITIZEN", "PERSONS", "WITH", "DISABILITY",
        "PHILHEALTH", "OSCA", "PWD", "SCID", "SOCIAL", "KASAMA KA", "Republic", "Philippines",
        "Province", "Unit", "Date of Issue", "Date", "Psychosocial", "ID. NO.", "ID. NO. "
    )
    return nameRegex.matches(line) && invalidKeywords.none { keyword ->
        line.split(" ").any { word -> word.equals(keyword, ignoreCase = true) }
    }
}

private fun extractIdNumber(text: String): String {
    val idRegex = listOf(
        Regex("""(?:Control\s*No\.|No\.|Control)\s*([\d-]{6,20})""", RegexOption.IGNORE_CASE),
        Regex("""(?:OSCA\s*I\.D\.No\.|ID\s*(?:No|Number|#)\D*)\s*([\d-]{10,20})""", RegexOption.IGNORE_CASE),
        Regex("""OSCA\s*ID:\s*(\d{3}-\d{7})""", RegexOption.IGNORE_CASE),
        Regex("""OSCA:\s*(\d{3}-\d{7})""", RegexOption.IGNORE_CASE),
        Regex("""\b(\d{2,4}-\d{4}-\d{3}-\d{5,7})\b"""),
        Regex("""\b(\d{4}-\d{4}-\d{4})\b"""),
        Regex("""\b(SC-\d{4}-\d{4})\b"""),
        Regex("""\b(\d{12,20})\b"""),
        Regex("""\b(\d{8})\b""")
    )
    idRegex.forEach { regex ->
        regex.find(text)?.groupValues?.get(1)?.let {
            return it.trim()
        }
    }
    return ""
}

private fun extractCity(text: String): String {
    val cityRegex = listOf(
        Regex("""(?:City|Lungsod|Municipality|Lalawigan|Government)\s+of\s+([A-Za-z]+)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Address:.*?)(\b[A-Za-z\s]+(?:City|Town))\b""", RegexOption.IGNORE_CASE),
        Regex("""(?:Residence:.*?)(\b[A-Za-z\s]+(?:City|Town))\b""", RegexOption.IGNORE_CASE)
    )
    cityRegex.forEach { regex ->
        regex.find(text)?.groupValues?.get(1)?.trim()?.let {
            return it.replace(Regex("""\bCity$""", RegexOption.IGNORE_CASE), "").trim()
        }
    }
    return ""
}

private fun extractFallbackIdNumber(text: String): String {
    val patterns = listOf(
        Regex("""(?:Control\s*No\.|No\.|Control)\s*([\d-]{6,20})""", RegexOption.IGNORE_CASE),
        Regex("""\b\d{4}[\s-]?\d{4}[\s-]?\d{4}\b"""),
        Regex("""\b\d{8,12}\b"""),
        Regex("""\bSC[\s-]?\d{4}[\s-]?\d{4}\b"""),
        Regex("""\bPWD[\s-]?\d{4}[\s-]?\d{4}\b""")
    )
    patterns.forEach { regex ->
        regex.find(text)?.value?.let {
            return it.replace(Regex("""[\s-]"""), "")
        }
    }
    return ""
}