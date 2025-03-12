package com.example.loginpage

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavController
) {
    val focusManager = LocalFocusManager.current
    val storeBranch = arrayOf("Dapitan", "España")
    val selectedStore = remember { mutableStateOf(storeBranch[0]) }
    val expanded = remember { mutableStateOf(false) }
    val nameInputRegistration = remember { mutableStateOf("") }
    val pinInputRegistration = remember { mutableStateOf("") }
    val usernameInputRegistration = remember { mutableStateOf("") }
    val context = LocalContext.current

    fun insertData(branchDb: String, nameDb: String, pinDb: String) {
        val url = "http://192.168.254.107/CalleCafe/mobile/registeredAccounts.php"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getBoolean("success")) {
                    Toast.makeText(context, "Registration Success! Please wait for the manager's account approval", Toast.LENGTH_SHORT).show()
                    navController.navigate("Routes.LoginScreen")
                } else {
                    Toast.makeText(context, "Insert Failed: ${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "Registration Failed! Please check your internet connection", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["branch"] = branchDb
                params["name"] = nameDb
                params["pin"] = pinDb
                return params
            }
        }

        requestQueue.add(stringRequest)
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
                // Static Top App Bar
                EmptyTopAppBar(navController = navController)
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

                Spacer(modifier = Modifier.height(30.dp))


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
                            text = "REGISTRATION",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(Color(0xFF8B4513), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Store Branch Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded.value,
                            onExpandedChange = { expanded.value = !expanded.value },
                            modifier = Modifier.border(BorderStroke(1.dp, Color.Black))
                        ) {
                            TextField(
                                value = selectedStore.value,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
                                },
                                label = { Text("Select Store Branch", color = Color.Black) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedLabelColor = Color.Transparent,
                                    focusedLabelColor = Color.Transparent
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false }
                            ) {
                                storeBranch.forEach { store ->
                                    DropdownMenuItem(
                                        text = { Text(store) },
                                        onClick = {
                                            selectedStore.value = store
                                            expanded.value = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name Input Field
                        OutlinedTextField(
                            value = nameInputRegistration.value,
                            onValueChange = { input ->
                                val filteredInput = input.filter {
                                    it.isLetter() || it == '.' || it == '-' || it.isWhitespace()
                                }
                                nameInputRegistration.value = filteredInput
                            },
                            label = { Text("Input Name") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        //Username Input Field
                        OutlinedTextField(
                            value = usernameInputRegistration.value,
                            onValueChange = { input ->
                                usernameInputRegistration.value = input
                            },
                            label = { Text("Input Username") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // PIN Input Field
                        OutlinedTextField(
                            value = pinInputRegistration.value,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() }) {
                                    pinInputRegistration.value = input
                                }
                            },
                            label = { Text("Input PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        val branchDb = selectedStore.value
                        val nameDb = nameInputRegistration.value
                        val pinDb = pinInputRegistration.value

                        navController.navigate("Routes.LoginScreen")
                        //****** IMPORTANT, Code to save data into the database

                        Log.d("DEBUG", "Branch: $branchDb, Name: $nameDb, Pin: $pinDb")

                        if (branchDb.isNotEmpty() && nameDb.isNotEmpty() && pinDb.isNotEmpty()) {
                            Log.d("DEBUG", "Inserting data")
                            insertData(branchDb, nameDb, pinDb)
                        } else {
                            Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                        }

                    },
                    modifier = Modifier
                        .bounceClick()
                        .height(70.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                ) {
                    Text(text = "SUBMIT", color = Color.White)
                }
            }
        }
    }
}


