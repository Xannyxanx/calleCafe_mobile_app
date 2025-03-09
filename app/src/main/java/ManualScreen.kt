package com.example.loginpage

import android.annotation.SuppressLint
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


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ManualScreen(navController: NavController) {
    val focusManager = LocalFocusManager.current
    val idNumberInputManual = remember { mutableStateOf("") }
    val nameInputManual = remember { mutableStateOf("") }
    val cityInputManual = remember { mutableStateOf("") }
    val disabilityOptions = listOf("Orthopedic", "Chronic", "Visual","Communication","Learning","Mental","Psychosocial")
    val selectedDisability = remember { mutableStateOf(disabilityOptions[0]) }
    val expanded = remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<String>() }
    val isPWDSelected = remember { mutableStateOf(false) }
    val isSeniorCitizenSelected = remember { mutableStateOf(false) }
    val isOthersSelected = remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler {
        navController.navigate("Routes.ScannerScreen") {
            popUpTo("Routes.ScannerScreen") { inclusive = true }
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
                                    Pair("Water", R.drawable.drinks),
                                    Pair("Pasta", R.drawable.pasta),
                                    Pair("Snacks", R.drawable.snacks)
                                )
                            ) { (description, drawableId) ->
                                val isSelected = selectedItems.contains(description)

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color(0xFF008000)else Color.Transparent,
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
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    idNumberInputManual.value = it
                                }
                            },
                            label = { Text("Input ID Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                                autoCorrect = false
                            ),
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
                                if (it.matches(Regex("^[A-Za-z.,-]*$"))) {
                                    nameInputManual.value = it
                                }
                            },
                            label = { Text("Input Fullname") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                                autoCorrect = false
                            ),
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
                                if (it.matches(Regex("^[A-Za-z.,-]*$"))) {
                                    cityInputManual.value = it
                                }
                            },
                            label = { Text("Input City") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                                autoCorrect = false
                            ),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Discount Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // PWD Button
                            Button(
                                onClick = {
                                    isPWDSelected.value = !isPWDSelected.value // Toggle PWD button
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPWDSelected.value) Color(0xFF6B4F3C) else Color(0xFF8B4513)
                                )
                            ) {
                                Text(text = "PWD", color = Color.White)
                            }

                            // Senior Citizen Button
                            Button(
                                onClick = {
                                    isSeniorCitizenSelected.value = !isSeniorCitizenSelected.value // Toggle Senior Citizen button
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSeniorCitizenSelected.value) Color(0xFF6B4F3C) else Color(0xFF8B4513)
                                )
                            ) {
                                Text(text = "Senior Citizen", color = Color.White)
                            }

                            // Others Button
                            Button(
                                onClick = {
                                    isOthersSelected.value = !isOthersSelected.value // Toggle Others button
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOthersSelected.value) Color(0xFF6B4F3C) else Color(0xFF8B4513)
                                )
                            ) {
                                Text(text = "Others", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Disability Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded.value,
                            onExpandedChange = { expanded.value = !expanded.value },
                            modifier = Modifier.border(BorderStroke(1.dp, Color.Black))
                        ) {
                            TextField(
                                value = selectedDisability.value,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                label = { Text("Type of Disability", color = Color.Black) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false },
                            ) {
                                disabilityOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedDisability.value = option
                                            expanded.value = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        handleSubmissionManual(
                            context = context,
                            navController = navController,
                            nameInput = nameInputManual.value,
                            pinInput = idNumberInputManual.value,
                            pwd = selectedDisability.value
                        )
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