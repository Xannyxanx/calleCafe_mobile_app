package com.example.loginpage

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URLDecoder
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConfirmationScreen(navController: NavController, name: String, idNumber: String, city: String, items: String, accountViewModel: AccountViewModel = viewModel()) {
    val context = LocalContext.current
    val transactionSuccessful by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) } // State for the confirmation dialog
    val nameDb = name
    val idNumberDb = idNumber
    val cityDb = city
    val decodedItems = URLDecoder.decode(items, "UTF-8")
    val accountHolder = accountViewModel.accountHolder.collectAsState().value
    val decodedName = URLDecoder.decode(name, "UTF-8")
    val decodedIdNumber = URLDecoder.decode(idNumber, "UTF-8")
    val decodedCity = URLDecoder.decode(city, "UTF-8")

    //Wag galawin
    fun insertData(idNumber: String, name: String, disability: String) {
        val url = "http://192.168.254.107/customers.php"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getBoolean("success")) {
                    Toast.makeText(context, "Transaction Successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate("Routes.ScannerScreen") {
                        popUpTo("Routes.ConfirmationScreen") { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Insert Failed: ${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "Transaction Failed! Please check your internet connection", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["idNumber"] = idNumber
                params["name"] = name
                params["city"] = city
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    // Disable back key
    BackHandler {
        Toast.makeText(context, "Back button disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBC8F5F))
    ) {
        Scaffold(
            topBar = {
                AppTopBar(navController = navController)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF5C4033))
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.loginpageimage),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(100.dp)
                        .width(100.dp)
                        .padding(bottom = 16.dp)
                        .alpha(0.5f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Main Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0C1A6))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Text(
                            text = "ORDER CONFIRMATION",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(Color(0xFF8B4513), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Images
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.defaultimage),
                                contentDescription = "Placeholder Image",
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(64.dp)
                                    .border(
                                        width = 2.dp,
                                        color = Color(0xFF8B4513),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                            Image(
                                painter = painterResource(id = R.drawable.defaultimage),
                                contentDescription = "Placeholder Image",
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(64.dp)
                                    .border(
                                        width = 2.dp,
                                        color = Color(0xFF8B4513),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Placeholder White Rectangle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(100.dp)
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "Name: $decodedName", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "ID Number: $decodedIdNumber", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "City: $decodedCity", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Food: $decodedItems", style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Adjusted Spacer for bottom elements
                Spacer(modifier = Modifier.height(62.dp))

                // Food Icon buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        modifier = Modifier
                            .bounceClick()
                            .height(48.dp),
                        onClick = {
                            navController.navigate("Routes.ScannerScreen") {
                                popUpTo("Routes.ScannerScreen") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDAA520))
                    ) {
                        Text(text = "EDIT", color = Color.White)
                    }

                    Button(
                        modifier = Modifier
                            .bounceClick()
                            .height(48.dp),
                        onClick = {
                            showConfirmDialog = true // Show the confirmation dialog
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                    ) {
                        Text(text = "CONFIRM", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // Alert box for confirm
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = "Alert") },
            text = { Text("Complete transaction?") },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("Routes.ScannerScreen")

                    //******** IMPORTANT, Code to input data from mobile app papunta sa database ********

                    Log.d("DEBUG", "IdNumber: $idNumberDb, Name: $nameDb, Disability: $cityDb")

                    if (idNumberDb.isNotEmpty() && nameDb.isNotEmpty() && cityDb.isNotEmpty()) {
                        Log.d("DEBUG", "Inserting data")
                        insertData(idNumberDb, nameDb, cityDb)
                    } else {
                        Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                    }

                    if (transactionSuccessful) {

                    } else {
                        Toast.makeText(context, "Transaction Canceled", Toast.LENGTH_SHORT).show()
                    }
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