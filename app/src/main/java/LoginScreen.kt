package com.example.loginpage

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

@Composable
fun LoginScreen(navController: NavController) {
        var showPinDialog by remember { mutableStateOf(false) }
        var pin by remember { mutableStateOf("") }
        val context = LocalContext.current
        var showUsernameDialog by remember { mutableStateOf(false) }
        var username by remember { mutableStateOf("") }

        fun selectData(pin: String) {
                val url = "http://192.168.254.107/CalleCafe/login.php"
                val requestQueue: RequestQueue = Volley.newRequestQueue(context)

                //Connection to database
                val stringRequest = object : StringRequest(
                        Request.Method.POST, url,
                        Response.Listener { response ->
                                try {
                                        Log.d("Response", response) // Log the raw response
                                        val jsonResponse = JSONObject(response)
                                        if (jsonResponse.getBoolean("success")) {
                                                val cashierName = jsonResponse.getJSONObject("user").getString("name")
                                                showPinDialog = false
                                                Toast.makeText(context, "Login Success!", Toast.LENGTH_SHORT).show()
                                                Log.d("Navigation", "Navigating to: Routes.ScannerScreen?cashierName=$cashierName")
                                                navController.navigate("Routes.ScannerScreen?cashierName=$cashierName") {
                                                        popUpTo("Routes.LoginScreen") { inclusive = true }
                                                }
                                        } else {
                                                Toast.makeText(context, "${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                                        }
                                } catch (e: Exception) {
                                        Log.e("JSONError", "Error parsing JSON: $response", e)
                                        Toast.makeText(context, "Unexpected response from server", Toast.LENGTH_SHORT).show()
                                }
                        },
                        Response.ErrorListener { error ->
                                Toast.makeText(context, "Login Failed! Please check your internet connection", Toast.LENGTH_SHORT).show()
                        }
                ) {
                        override fun getParams(): MutableMap<String, String> {
                                val params = HashMap<String, String>()
                                params["pin"] = pin
                                return params
                        }

                }

                requestQueue.add(stringRequest)
        }


        if (showUsernameDialog) {
                AlertDialog(
                        onDismissRequest = { showUsernameDialog = false },
                        title = { Text(text = "Enter Username") },
                        text = {
                                Column {
                                        TextField(
                                                value = username,
                                                onValueChange = { username = it },
                                                label = { Text("Username") },
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                }
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                if (username.isNotBlank()) {
                                                        // Navigate to PinInputScreen with username as parameter
                                                        navController.navigate("Routes.PinInputScreen/$username")
                                                        showUsernameDialog = false
                                                } else {
                                                        Toast.makeText(context, "Please enter your username", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                ) {
                                        Text("Continue")
                                }
                        },
                        dismissButton = {
                                Button(
                                        onClick = { showUsernameDialog = false }
                                ) {
                                        Text("Cancel")
                                }
                        }
                )
        }


        BackHandler {
                // Exit the app when back is pressed on LoginScreen
                (context as? Activity)?.finish()
        }

        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF9C6F44)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Image( // Logo
                        painter = painterResource(id = R.drawable.loginpageimage),
                        contentDescription = "Login page image",
                        modifier = Modifier
                                .alpha(0.7f)
                                .height(350.dp)
                                .width(350.dp)
                                .padding(bottom = 16.dp)
                )


                Spacer(modifier = Modifier.height(32.dp))

                // Sign-in button
                Button(
                        onClick = {
                                showUsernameDialog = true
                        },
                        modifier = Modifier
                                .bounceClick()
                                .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF008000),
                                contentColor = Color.White
                        )
                ) {
                        Text(
                                text = "SIGN-IN",
                                fontWeight = FontWeight.Bold
                        )
                }

                Spacer(modifier = Modifier.height(100.dp))

                // Text button for registration
                TextButton(
                        onClick = {
                                navController.navigate("Routes.RegistrationScreen")
                        },
                        modifier = Modifier.bounceClick()
                ) {
                        Text(
                                text = "Register new account",
                                color = Color.White
                        )
                }

//                //PIN input na need ayusin yung UI
//                if (showPinDialog) {
//                        AlertDialog(
//                                onDismissRequest = { showPinDialog = false },
//                                title = { Text(text = "Enter PIN") },
//                                text = {
//                                        Column {
//                                                TextField(
//                                                        value = pin,
//                                                        onValueChange = { newValue ->
//                                                                // Allow only numeric input
//                                                                if (newValue.all { it.isDigit() }) pin = newValue
//                                                        },
//                                                        label = { Text("PIN") },
//                                                        visualTransformation = PasswordVisualTransformation(),
//                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
//                                                )
//                                        }
//                                },
//
//                                /*"okay" button to enter the PIN. copy paste mo lang siguro tong statement na nasa ilalim neto
//                                dun sa submit or enter button  na gagawin mo kapag ginawan mo siya ng panibagong UI paki include
//                                na din yung naka comment na important*/
//
//                                confirmButton = {
//                                        Button(
//                                                onClick = {
//                                                        navController.navigate("Routes.ScannerScreen")
//                                                        //!!!!!!!! IMPORTANT, CODE TO GET DATA FROM THE DATABASE !!!!!!!!!!
//
//                                                        /*
//                                                        if (pin.isNotEmpty()) {
//                                                                Log.d("DEBUG", "Selecting data") // Log before inserting
//                                                                selectData(pin)
//                                                        } else {
//                                                                Toast.makeText(context, "Please enter your PIN", Toast.LENGTH_SHORT).show()
//                                                        }
//                                                         */
//
//                                                }
//                                        ) {
//                                                Text("OK")
//                                        }
//                                },
//                                dismissButton = {
//                                        Button(
//                                                onClick = { showPinDialog = false }
//                                        ) {
//                                                Text("Cancel")
//                                        }
//                                }
//                        )
//                }
        }
}




