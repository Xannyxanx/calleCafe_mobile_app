package com.example.loginpage

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ManualScreen(
    navController: NavController,
    accountViewModel: AccountViewModel = viewModel(),
    selectedItemsFromScanner: List<String> = emptyList(),
    prefilled: String? = null
) {
    val accountHolder = accountViewModel.accountHolder.collectAsState().value
    val focusManager = LocalFocusManager.current
    val idNumberInputManual = remember { mutableStateOf("") }
    val nameInputManual = remember { mutableStateOf("") }
    val cityInputManual = remember { mutableStateOf("") }
    val isPWDSelected = remember { mutableStateOf(false) }
    val isSeniorCitizenSelected = remember { mutableStateOf(false) }
    val isOthersSelected = remember { mutableStateOf(false) }
    val selectedCitizenType = remember { mutableStateOf("") } // Changed to mutableStateOf("")
    val context = LocalContext.current
    val selectedItems = remember { mutableStateListOf<String>().apply {
        addAll(selectedItemsFromScanner)
    } }

    val gson = Gson()

    val preFilledData = remember {
        try {
            prefilled?.let {
                val decoded = URLDecoder.decode(it, "UTF-8")
                gson.fromJson<Map<String, String>>(
                    decoded,
                    object : TypeToken<Map<String, String>>() {}.type
                )
            } ?: emptyMap()
        } catch (e: Exception) {
            emptyMap<String, String>()
        }
    }

    BackHandler {
        val previousRoute = navController.previousBackStackEntry?.destination?.route
        if (previousRoute == "Routes.LoginScreen" || previousRoute == "Routes.PinInputScreen") {
            // Pop the ScannerScreen from the stack inclusively
            navController.popBackStack(route = "Routes.LoginScreen", inclusive = true)
            // Exit the app
            (context as? android.app.Activity)?.finishAffinity() // Graceful exit
        } else {
            // Otherwise, navigate back
            navController.popBackStack()
        }
    }

    LaunchedEffect(preFilledData) {
        if (preFilledData.isNotEmpty()) {
            idNumberInputManual.value = preFilledData["idNumber"] ?: ""
            nameInputManual.value = preFilledData["name"] ?: ""
            cityInputManual.value = preFilledData["city"] ?: ""
            
            val itemsList = preFilledData["selectedItems"]?.split(",") ?: emptyList()
            selectedItems.clear()
            selectedItems.addAll(itemsList)

            when (preFilledData["citizenType"]) {
                "PWD" -> {
                    isPWDSelected.value = true
                    selectedCitizenType.value = "PWD"
                }
                "Senior Citizen" -> {
                    isSeniorCitizenSelected.value = true
                    selectedCitizenType.value = "Senior Citizen"
                }
                "Others" -> {
                    isOthersSelected.value = true
                    selectedCitizenType.value = "Others"
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Scaffold(
            topBar = {
                AppTopBarWithBack(navController = navController)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF5C4033)) // Background color
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main content card
                Card(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0C1A6))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Text(
                            text = "MANUAL ENTRY",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(Color(0xFF8B4513), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Food Selection
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            items(
                                listOf(
                                    Pair("Drinks", R.drawable.drinks),
                                    Pair("Pasta", R.drawable.pasta),
                                    Pair("Pastry", R.drawable.snacks)
                                )
                            ) { (description, drawableId) ->
                                val isSelected = selectedItems.contains(description)

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color(0xFF008000) else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            if (isSelected) {
                                                selectedItems.remove(description)
                                            } else {
                                                selectedItems.add(description)
                                            }
                                        }
                                ) {
                                    val alphaValue by animateFloatAsState(if (isSelected) 0.5f else 1f)

                                    Icon(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = description,
                                        modifier = Modifier
                                            .height(64.dp)
                                            .width(64.dp)
                                            .alpha(alphaValue),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Text fields
                        OutlinedTextField(
                            value = idNumberInputManual.value,
                            onValueChange = { idNumberInputManual.value = it },
                            label = { Text("Input ID Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done, autoCorrect = false),
                            colors = outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        OutlinedTextField(
                            value = nameInputManual.value,
                            onValueChange = {
                                if (it.matches(Regex("^[A-Za-z .\\-]*$"))) { // Allow only letters and spaces
                                    nameInputManual.value = it
                                }
                            },
                            label = { Text("Input Fullname") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done, autoCorrect = false),
                            colors = outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        OutlinedTextField(
                            value = cityInputManual.value,
                            onValueChange = {
                                if (it.matches(Regex("^[A-Za-z ]*$"))) {
                                    cityInputManual.value = it
                                }
                            },
                            label = { Text("Input City") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done, autoCorrect = false),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Discount Buttons
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // First row for PWD and Senior Citizen
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // PWD Button
                                Button(
                                    onClick = {
                                        if (isPWDSelected.value) {
                                            selectedCitizenType.value = ""
                                            isPWDSelected.value = false
                                        } else {
                                            selectedCitizenType.value = "PWD"
                                            isPWDSelected.value = true
                                            isSeniorCitizenSelected.value = false
                                            isOthersSelected.value = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPWDSelected.value) Color(0xFF6B4F3C) else Color(0xFF8B4513)
                                    )
                                ) {
                                    Text(text = "PWD", color = Color.White)
                                }

                                // Senior Citizen Button
                                Button(
                                    onClick = {
                                        if (isSeniorCitizenSelected.value) {
                                            selectedCitizenType.value = ""
                                            isSeniorCitizenSelected.value = false
                                        } else {
                                            selectedCitizenType.value = "Senior Citizen"
                                            isSeniorCitizenSelected.value = true
                                            isPWDSelected.value = false
                                            isOthersSelected.value = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSeniorCitizenSelected.value) Color(0xFF6B4F3C) else Color(0xFF8B4513)
                                    )
                                ) {
                                    Text(text = "Senior Citizen", color = Color.White)
                                }
                            }

                            // Space between rows
                            Spacer(modifier = Modifier.height(8.dp))

                            // Others Button
                            Button(
                                onClick = {
                                    if (isOthersSelected.value) {
                                        selectedCitizenType.value = ""
                                        isOthersSelected.value = false
                                    } else {
                                        selectedCitizenType.value = "Others"
                                        isOthersSelected.value = true
                                        isPWDSelected.value = false
                                        isSeniorCitizenSelected.value = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp), // Set fixed height
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOthersSelected.value) Color(0xFF6B4F3C) else Color(0xFF8B4513)
                                )
                            ) {
                                Text(text = "Others", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (idNumberInputManual.value.isBlank() ||
                            nameInputManual.value.isBlank() ||
                            cityInputManual.value.isBlank() ||
                            selectedItems.isEmpty() ||
                            selectedCitizenType.value.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please fill all fields, select items and choose a citizen type",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Format the selected items
                            val formattedItems = formatSelectedItems(selectedItems)
                            
                            // Create the data string properly
                            val data = listOf(
                                "CitizenType=${selectedCitizenType.value}",
                                "Items=$formattedItems"
                            ).joinToString("&")
                            
                            // URL encode the data
                            val encodedData = URLEncoder.encode(data, "UTF-8")
                            
                            val name = URLEncoder.encode(nameInputManual.value, "UTF-8")
                            val idNumber = URLEncoder.encode(idNumberInputManual.value, "UTF-8")
                            val city = URLEncoder.encode(cityInputManual.value, "UTF-8")
                            
                            // Navigate with the encoded values
                            navController.navigate("Routes.ConfirmationScreen/$name/$idNumber/$city/$encodedData")
                        }
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                        .bounceClick(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                ) {
                    Text(text = "SUBMIT", color = Color.White)
                }
            }
        }
    }
}

private fun formatSelectedItems(items: List<String>): String {
    // Define the desired order
    val order = listOf("Drinks", "Pasta", "Pastry")
    
    // Sort and filter items based on the defined order
    return items
        .filter { it in order } // Only keep items that are in our order list
        .sortedBy { order.indexOf(it) } // Sort based on the defined order
        .joinToString(",")
}