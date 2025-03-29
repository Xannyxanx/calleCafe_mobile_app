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
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Base64
import android.view.Surface
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginpage.AccountHolder
import com.example.loginpage.AccountViewModel
import org.junit.runner.manipulation.Ordering
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(navController: NavController, accountViewModel: AccountViewModel = viewModel()) {
    val accountHolder = accountViewModel.accountHolder.collectAsState().value
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val isScanning = remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<String>() }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                startCamera(cameraProviderFuture, lifecycleOwner, previewView, context, cameraExecutor, isScanning, navController, selectedItems)
            } else {
                // Handle permission denial
            }
        }
    )
    val mainActivity = LocalContext.current as MainActivity

    LaunchedEffect(Unit) {
        mainActivity.requestedOrientation =
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    DisposableEffect(Unit) {
        onDispose {
            mainActivity.requestedOrientation =
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(cameraProviderFuture, lifecycleOwner, previewView, context, cameraExecutor, isScanning, navController, selectedItems)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cashierName = navController.currentBackStackEntry?.arguments?.getString("cashierName")
    Log.d("ScannerScreen", "Cashier Name: $cashierName")

    BackHandler {
        val previousRoute = navController.previousBackStackEntry?.destination?.route
        if (previousRoute == "Routes.LoginScreen" || previousRoute == "Routes.PinInputScreen") {
            // Clear the entire stack
            navController.popBackStack(route = "Routes.LoginScreen", inclusive = true)
            // Exit the app
            (context as? Activity)?.finishAffinity()
        } else {
            // Otherwise, navigate back
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                cashierName = accountHolder?.name,
                cashierBranch = accountHolder?.branch
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF5C4033))
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        mainActivity.resetTimer() // Use MainActivity's timer reset
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Camera preview
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CameraPreviewContent(previewView = previewView)
            }

            // Right side - Controls and info
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cashier: ${accountHolder?.name ?: "No User"}",
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Branch: ${accountHolder?.branch ?: "No User"}",
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        if (selectedItems.isNotEmpty()) {
                            isScanning.value = true
                        } else {
                            Log.d("ScannerScreen", "No selected items. Cannot start scanning.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF008000),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .bounceClick()
                ) {
                    Text(text = "SCAN ID", fontWeight = FontWeight.Bold)
                }

                // Food Selection
                val itemsList = listOf(
                    Pair("Drinks", R.drawable.drinks),
                    Pair("Pasta", R.drawable.pasta),
                    Pair("Pastry", R.drawable.snacks)
                )
                val selectedStates = remember { itemsList.map { mutableStateOf(false) } }

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
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(itemsList) { (description, drawableId) ->
                            val index = itemsList.indexOfFirst { it.first == description }
                            val isSelected = selectedStates[index]
                            val alphaValue by animateFloatAsState(if (isSelected.value) 0.5f else 1f)

                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        isSelected.value = !isSelected.value
                                        if (isSelected.value) {
                                            selectedItems.add(description)
                                        } else {
                                            selectedItems.remove(description)
                                        }
                                        mainActivity.resetTimer()
                                    }
                                    .border(
                                        width = if (isSelected.value) 2.dp else 0.dp,
                                        color = if (isSelected.value) Color(0xFF008000) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(id = drawableId),
                                    contentDescription = description,
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .alpha(alphaValue)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val pwdKeywords = listOf(
    "PWD", "PERSONS WITH DISABILITY", "DISABILITY", "PWD ID", "PWD IDENTIFICATION",
    "PHILHEALTH", "DISABILITY ID", "DISABILITY CARD", "PSYCHOSOCIAL", "MENTAL", "PHYSICAL", "VISUAL",
    "HEARING", "LEARNING", "SPEECH", "ORTHOPEDIC", "VISION", "IMPAIRMENT"
)

private val seniorCitizenKeywords = listOf(
    "SENIOR CITIZEN", "OSCA", "SENIOR CITIZEN ID", "OSCA ID", "SENIOR CITIZEN CARD",
    "OFFICE OF THE SENIOR CITIZENS AFFAIRS", "OSCA IDENTIFICATION"
)

private fun detected(visionText: Text): Boolean {
    val allKeywords = pwdKeywords + seniorCitizenKeywords
    val text = visionText.text
    return allKeywords.any { keyword -> text.contains(keyword, ignoreCase = true) }
}

private fun processText(visionText: Text, context: android.content.Context, navController: NavController, selectedItems: List<String>) {
    if (!detected(visionText)) {
        Log.d("ProcessText", "Not a PWD or Senior Citizen ID. Skipping extraction.")
        Toast.makeText(context, "No PWD/Senior Citizen ID Detected, Proceeding to Manual Input", Toast.LENGTH_LONG).show()
        return

    }

    val citizenType = when {
        pwdKeywords.any { keyword -> visionText.text.contains(keyword, ignoreCase = true) } -> "PWD"
        seniorCitizenKeywords.any { keyword -> visionText.text.contains(keyword, ignoreCase = true) } -> "Senior Citizen"
        else -> ""
    }

    val fullText = visionText.textBlocks.joinToString("\n") { it.text }
    val name = extractName(fullText)
    var idNumber = extractIdNumber(fullText)
    val city = extractCity(fullText)

    val formattedItems = formatSelectedItems(selectedItems)



    val data = listOf(
        "CitizenType=$citizenType",
        "Items=$formattedItems"
    ).joinToString("&")



    Log.d("ProcessText", "Extracted Name: $name")
    Log.d("ProcessText", "Extracted ID Number: $idNumber")
    Log.d("ProcessText", "Extracted City: $city")
    Log.d("ProcessText", "Citizen Type: $citizenType")
    Log.d("ProcessText", "Formatted Items: $formattedItems")

    val encodedData = URLEncoder.encode(data, "UTF-8")
    navController.navigate("Routes.ConfirmationScreen/$name/$idNumber/$city/$encodedData")
}

private fun startCamera(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    context: android.content.Context,
    cameraExecutor: ExecutorService,
    isScanning: MutableState<Boolean>,
    navController: NavController,
    selectedItems: List<String>
) {
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            Log.d("CameraSetup", "Got camera provider")

            // Set up the preview use case
            val preview = Preview.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)
            Log.d("CameraSetup", "Preview surface provider set")

            // Set up image analysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetRotation(previewView.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                if (isScanning.value) {
                    processImageForTextRecognition(
                        imageProxy,
                        context,
                        isScanning,
                        navController,
                        selectedItems
                    )
                } else {
                    imageProxy.close()
                }
            }

            // Select back camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                Log.d("CameraSetup", "Attempting to unbind use cases")
                cameraProvider.unbindAll()
                Log.d("CameraSetup", "Attempting to bind use cases")
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                Log.d("CameraSetup", "Camera use cases bound successfully")
            } catch (exc: Exception) {
                Log.e("CameraSetup", "Use case binding failed", exc)
            }

        } catch (exc: Exception) {
            Log.e("CameraSetup", "Camera initialization failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))
}

@Composable
private fun CameraPreviewContent(previewView: PreviewView) {
    AndroidView(
        factory = { context ->
            previewView.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageForTextRecognition(
    imageProxy: ImageProxy,
    context: android.content.Context,
    isScanning: MutableState<Boolean>,
    navController: NavController,
    selectedItems: List<String>
) {
    try {
        val mediaImage = imageProxy.image ?: run {
            Log.e("ImageCapture", "Media image is null")
            imageProxy.close()
            return
        }

        // 1. Capture image as bitmap first
        val bitmap = try {
            captureImageBitmap(mediaImage, imageProxy)
        } catch (e: Exception) {
            Log.e("ImageCapture", "Failed to capture image", e)
            imageProxy.close()
            return
        }

        // 2. Convert bitmap to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        if (imageBytes.size > 1) {
            val header = String.format("%02X%02X", imageBytes[0], imageBytes[1])
            Log.d("JPEG_CHECK", "Header: $header") // Should output "FFD8"
        }
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        // Validate the image
        if (base64Image.isEmpty()) {
            Log.e("ImageCapture", "Empty Base64 image generated!")
            Toast.makeText(context, "Failed to capture ID image", Toast.LENGTH_SHORT).show()
            imageProxy.close()
            return
        }

        // 3. Debug logging
        Log.d("ImageDebug", "Image size: ${imageBytes.size} bytes")
        Log.d("ImageDebug", "Base64 length: ${base64Image.length}")

        // 4. Create InputImage from bitmap (safer than mediaImage)
        val inputImage = InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)

        // 5. Process text recognition
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                try {
                    val fullText = visionText.text
                    val name = extractName(fullText)
                    var idNumber = extractIdNumber(fullText)
                    val city = extractCity(fullText)
                    if (idNumber.isBlank()) {
                        idNumber = extractFallbackIdNumber(fullText)
                    }

                    if (detected(visionText)) {
                        // Create navigation data with extracted information
                        val data = listOf(
                            "CitizenType=${getCitizenType(visionText)}",
                            "Items=${formatSelectedItems(selectedItems)}",
                            "CustomerID=$base64Image" // Add image to data
                        ).joinToString("&")

                        // Navigate to ConfirmationScreen
                        val encodedData = URLEncoder.encode(data, "UTF-8")
                        navController.navigate("Routes.ConfirmationScreen/$name/$idNumber/$city/$encodedData")
                        Log.d("Navigation", "Navigating with image and text data")
                    } else {
                        // Handle manual navigation with image
                        CoroutineScope(Dispatchers.Main).launch {
                            val encodedItems = URLEncoder.encode(selectedItems.joinToString(","), "UTF-8")
                            navController.navigate("Routes.ManualScreen?selectedItems=$encodedItems&customerID=${URLEncoder.encode(base64Image, "UTF-8")}")
                            Log.d("Navigation", "Navigating to ManualScreen with image")
                        }
                    }
                } finally {
                    isScanning.value = false
                    imageProxy.close()
                }
            }
            .addOnFailureListener { e ->
                Log.e("TextRecognition", "Failed to process image", e)
                // Handle failure and navigate to manual input with image
                CoroutineScope(Dispatchers.Main).launch {
                    val encodedItems = URLEncoder.encode(selectedItems.joinToString(","), "UTF-8")
                    navController.navigate("Routes.ManualScreen?selectedItems=$encodedItems&customerID=${URLEncoder.encode(base64Image, "UTF-8")}")
                    Log.d("Navigation", "Failed processing, navigating to ManualScreen with image")
                }
                isScanning.value = false
                imageProxy.close()
            }
    } catch (e: Exception) {
        Log.e("TextRecognition", "Unexpected error", e)
        // Always navigate to manual input on error
        CoroutineScope(Dispatchers.Main).launch {
            val encodedItems = URLEncoder.encode(selectedItems.joinToString(","), "UTF-8")
            navController.navigate("Routes.ManualScreen?selectedItems=$encodedItems")
        }
        isScanning.value = false
        imageProxy.close()
    }
}

private fun getCitizenType(visionText: Text): String {
    return when {
        pwdKeywords.any { keyword -> visionText.text.contains(keyword, ignoreCase = true) } -> "PWD"
        seniorCitizenKeywords.any { keyword -> visionText.text.contains(keyword, ignoreCase = true) } -> "Senior Citizen"
        else -> "Unknown"
    }
}
private fun captureImageBitmap(
    mediaImage: android.media.Image,
    imageProxy: ImageProxy
): Bitmap {
    val yBuffer = mediaImage.planes[0].buffer
    val uBuffer = mediaImage.planes[1].buffer
    val vBuffer = mediaImage.planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, mediaImage.width, mediaImage.height, null)
    val outputStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, mediaImage.width, mediaImage.height), 70, outputStream)
    return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
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

private fun formatSelectedItems(items: List<String>): String {
    val order = listOf("Drinks", "Pasta", "Pastry")
    return items
        .filter { it in order }
        .sortedBy { order.indexOf(it) }
        .joinToString(",")
}