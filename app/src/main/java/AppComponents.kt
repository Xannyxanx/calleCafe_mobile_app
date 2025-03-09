package com.example.loginpage

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Horizontal
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)//app bar
@Composable
fun AppTopBar(
    title: String = "Calle Cafe",
    navController: NavController,
    cashierName: String? = "Unknown"
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFDAA520), // Top bar color
            titleContentColor = Color.White
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily(Font(R.font.poppinssemibold)),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(8.dp))
            }
        },
        actions = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFF8B4513)
                    )
                }

                // Hamburger menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            Log.d("Navigation", "Navigating to: Routes.PinAccountInputScreen?cashierName=$cashierName")
                            navController.navigate("Routes.PinAccountInputScreen?cashierName=$cashierName")
                        },
                        text = { Text("Account") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            showConfirmDialog = true
                            expanded = false
                        },
                        text = { Text("Sign Out") }
                    )
                }
            }
        }
    )

    // Alert box
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = "Alert") },
            text = { Text("Do you want to sign out?") },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("Routes.LoginScreen")
                    showConfirmDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showConfirmDialog = false
                }) {
                    Text("No")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)//accounts screen discount
@Composable
fun DiscountField(label: String, discount: MutableState<String>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = discount.value,
            onValueChange = {
                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                    discount.value = it
                }
            },
            label = { Text(label) },
            modifier = Modifier.width(100.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Done),
            colors = outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black  )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable //alternative app bar no accounts access
fun AppTopBarWithBack(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "Back to Scanner",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate("Routes.ScannerScreen") {
                    popUpTo("Routes.ScannerScreen") { inclusive = true }
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Scanner Screen",
                    tint = Color(0xFF8B4513)
                )
            }
        },
        actions = {
            IconButton(onClick = {
                showLogoutDialog = true
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "User Icon",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFFDAA520))
    )

    // alert for logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(text = "Alert")
            },
            text = {
                Text("Do you want to sign-out?")
            },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigate("Routes.LoginScreen") {
                        popUpTo("Routes.LoginScreen") { inclusive = true }
                    }
                    showLogoutDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                }) {
                    Text("No")
                }
            }
        )
    }
}

enum class ButtonState { Pressed, Idle }//bounce click
fun Modifier.bounceClick() = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(if (buttonState == ButtonState.Pressed) 0.70f else 1f)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {  }
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonState.Pressed
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)//registration screen top bar
@Composable
fun EmptyTopAppBar(navController: NavController) {
    TopAppBar(
        title = {

            Text(
                text = "Back to Sign-in Page",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate("Routes.LoginScreen") {
                    popUpTo("Routes.LoginScreen") { inclusive = true }
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Login Screen",
                    tint = Color(0xFF8B4513)
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFFDAA520))
    )
}

@OptIn(ExperimentalMaterial3Api::class)//registration screen top bar
@Composable
fun PinTopAppBar(navController: NavController) {
    TopAppBar(
        title = {

            Text(
                text = "",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFFDAA520))
    )
}


fun handleSubmissionRegistration  (//registration screen button logic
    context: Context,
    navController: NavController,
    nameInput: String,
    pinInput: String,
    successMessage: String = "Success. Wait for account approval.",
    errorMessage: String = "Please fill in all fields",
    destination: String = "Routes.LoginScreen"
) {
    if (nameInput.isNotEmpty() && pinInput.isNotEmpty()) {
        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
        navController.navigate(destination) {
            popUpTo(destination) { inclusive = true }
        }
    } else {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

fun handleSubmissionManual(//manual entry screen button logic
    context: Context,
    navController: NavController,
    nameInput: String,
    pinInput: String,
    pwd: String,
    successMessage: String = "",
    errorMessage: String = "Please fill in all fields",
    destination: String = "Routes.ConfirmationScreen"
) {
    if (nameInput.isNotEmpty() && pinInput.isNotEmpty()) {
        val route = "Routes.ConfirmationScreen/$nameInput/$pinInput/$pwd"
        Log.d("handleSubmissionManual", "Navigating to route: $route")
        navController.navigate(route)
    } else {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun OtpTextField(otpText: String, onValueChange: (String) -> Unit) {//pin input
    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                onValueChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
////        keyboardActions = KeyboardActions(
//////            onDone = { /*pwede ilagay dito button kung sakali */ }
//        ),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val number = when {
                        index >= otpText.length -> ""
                        else -> "*"
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = number,
                            color = Color(0xFF8B4513),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(Color(0xFF8B4513))
                        )
                    }
                }
            }
        }
    )
}






