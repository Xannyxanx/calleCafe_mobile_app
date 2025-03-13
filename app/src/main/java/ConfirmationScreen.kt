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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginpage.DiscountPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLEncoder

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConfirmationScreen(navController: NavController, name: String, idNumber: String, city: String, items: String, accountViewModel: AccountViewModel = viewModel()) {
    val context = LocalContext.current
    val transactionSuccessful by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val nameDb = name
    val idNumberDb = idNumber
    val cityDb = city
    val decodedItems = URLDecoder.decode(items, "UTF-8")
    val accountHolder = accountViewModel.accountHolder.collectAsState().value
    val decodedName = URLDecoder.decode(name, "UTF-8")
    val decodedIdNumber = URLDecoder.decode(idNumber, "UTF-8")
    val decodedCity = URLDecoder.decode(city, "UTF-8")

    //PAG DECODE NG ITEMS FOR UTF=8
    val decodedData = URLDecoder.decode(items, "UTF-8").split("&")
    val dataMap = decodedData.associate {
        val parts = it.split("=")
        parts[0] to parts[1]
    }
    
    val citizenType = dataMap["CitizenType"] ?: ""
    val decodedItemsList = dataMap["Items"] ?: ""

    val discountPrefs = remember { DiscountPreferences(context) }
    val discountPercentage = remember { mutableStateOf(0f) }

    //PAG KUHA NG ITEMS IF PWD BA OR SENIOR CITIZENS
    LaunchedEffect(citizenType) {
        discountPercentage.value = when (citizenType) {
            "PWD" -> discountPrefs.getDiscountPercentage("pwd")
            "Senior Citizen" -> discountPrefs.getDiscountPercentage("senior")
            "Others" -> discountPrefs.getDiscountPercentage("others")
            else -> 0f
        }
    }

    // Function to handle edit button click
    fun handleEditButtonClick() {
        // Create the pre-filled data
        val preFilledData = mapOf(
            "idNumber" to decodedIdNumber,
            "name" to decodedName,
            "city" to decodedCity,
            "selectedItems" to decodedItemsList,
            "citizenType" to citizenType
        )

        // Convert data to URL encoded string
        val encodedData = URLEncoder.encode(Gson().toJson(preFilledData), "UTF-8")
        
        // Navigate to ManualScreen with the pre-filled data
        navController.navigate("Routes.ManualScreen?prefilled=$encodedData")
    }

    data class TransactionData(
        val idNumber: String,
        val name: String,
        val city: String,
        val citizenType: String,
        val items: String,
        val date: String,
        val time: String,
        val cashierName: String,
        val branch: String,
        val discountPercentage: Float
    )

    fun insertData(data: TransactionData) {
        val url = "http://192.168.254.107/CalleCafe/mobile/Insertcustomers.php"
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
                params["idNumber"] = data.idNumber
                params["name"] = data.name
                params["city"] = data.city
                params["citizenType"] = data.citizenType
                params["food"] = data.items
                params["date"] = data.date
                params["time"] = data.time
                params["cashierName"] = data.cashierName
                params["branch"] = data.branch
                params["discountPercentage"] = data.discountPercentage.toString()
                return params
            }
        }

        requestQueue.add(stringRequest)
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
                        .height(80.dp)
                        .width(80.dp)
                        .padding(bottom = 16.dp)
                        .alpha(0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Main Content Card
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()) // Add scroll if content is too long
                        .weight(1f, fill = true)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                text = "ORDER CONFIRMATION",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color(0xFF8B4513), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // White Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(text = "Name: $decodedName", style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "ID Number: $decodedIdNumber", style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "City: $decodedCity", style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Citizen Type: $citizenType", style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Food: $decodedItemsList", style = MaterialTheme.typography.bodyLarge)
                                    if (discountPercentage.value > 0f) {
                                        Text(
                                            text = "Discount: ${discountPercentage.value}%",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Green
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Buttons at the bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                            .height(48.dp),
                        onClick = {
                            handleEditButtonClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDAA520))
                    ) {
                        Text(text = "EDIT", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                            .height(48.dp),
                        onClick = {
                            showConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                    ) {
                        Text(text = "CONFIRM", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp)) // Add extra space at the bottom
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
                    // Get the current account holder
                    val account = accountHolder ?: return@Button
                    
                    // Get current date and time
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                    // Prepare all data to be inserted
                    val dataToInsert = TransactionData(
                        idNumber = idNumberDb,
                        name = nameDb,
                        city = cityDb,
                        citizenType = citizenType,
                        items = decodedItemsList,
                        date = currentDate,
                        time = currentTime,
                        cashierName = account.name,
                        branch = account.branch,
                        discountPercentage = discountPercentage.value
                    )

                    // Log all data for debugging
                    Log.d("INSERT_DATA", "Data to be inserted: $dataToInsert")

                    // Insert the data
                    insertData(dataToInsert)
                    
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