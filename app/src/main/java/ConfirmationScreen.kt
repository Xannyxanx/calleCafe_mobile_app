package com.example.loginpage

import DiscountManager
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import java.util.UUID
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLEncoder
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.YuvImage
import android.util.Base64
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConfirmationScreen(navController: NavController, name: String, idNumber: String, city: String, items: String, accountViewModel: AccountViewModel = viewModel(), discountManager: DiscountManager) {
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
    val customerID = dataMap["CustomerID"] // Get the customer ID from dataMap

    val discountPercentage = remember(citizenType) { 
        mutableStateOf(discountManager.getDiscountPercentage(citizenType))
    }

    var priceInput by remember { mutableStateOf("") }
    var controlNumber by remember { mutableStateOf("") }
    val priceValue = remember(priceInput, discountPercentage.value) {
        val price = priceInput.toFloatOrNull() ?: 0f
        price * (1 - discountPercentage.value / 100)
    }

    // Remove all LaunchedEffect/PWD/Senior Citizen discount logic
    // Remove any references to DiscountPreferences

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
        val discountPercentage: Float,
        val controlNo: String,
        val originalPrice: Float,
        val totalPrice: Float,
        val customerID: ByteArray? = null // Add nullable customer ID
    )

    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateFoodItems by remember { mutableStateOf<List<String>>(emptyList()) }
    var existingOrderInfo by remember { mutableStateOf<Map<String, String>?>(null) }

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
                    val errorMessage = jsonResponse.getString("message")
                    if (errorMessage.contains("already been availed")) {
                        val parts = errorMessage.split("\n\nComplete order information:")
                        val duplicateItemsPart = parts[0].replace("The following food items have already been availed:\n• ", "")
                        duplicateFoodItems = duplicateItemsPart.split("\n• ")

                        existingOrderInfo = parts.getOrNull(1)
                            ?.split("\n")
                            ?.mapNotNull { line ->
                                line.split(": ", limit = 2).takeIf { it.size == 2 }?.let {
                                    it[0].trim() to it[1].trim()
                                }
                            }
                            ?.toMap()

                        showDuplicateDialog = true
                    } else {
                        Toast.makeText(context, "Insert Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
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
                params["control_no"] = data.controlNo
                params["original_price"] = data.originalPrice.toString()
                params["total_price"] = data.totalPrice.toString()
                data.customerID?.let {
                    params["customerID"] = Base64.encodeToString(it, Base64.DEFAULT)
                }
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
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF5C4033))
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Section (Compact)
                Image(
                    painter = painterResource(id = R.drawable.loginpageimage),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(vertical = 8.dp)
                        .alpha(0.5f)
                )

                // Main Content Card (Optimized Height)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0C1A6))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Order Info Section (Tighter Padding)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "ORDER CONFIRMATION",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF8B4513),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // White Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)  // Reduced height
                                    .background(Color.White, RoundedCornerShape(8.dp))
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

                                    // Display the customer ID image
                                    val decodedCustomerID = remember(customerID) {
                                        if (customerID != null) {
                                            try {
                                                Base64.decode(customerID, Base64.DEFAULT)
                                            } catch (e: Exception) {
                                                Log.e("DECODE_ERROR", "Failed to decode customer ID", e)
                                                null
                                            }
                                        } else {
                                            null
                                        }
                                    }

                                    decodedCustomerID?.let { safeDecodedId ->
                                        val bitmap = remember(safeDecodedId) {
                                            try {
                                                BitmapFactory.decodeByteArray(safeDecodedId, 0, safeDecodedId.size)
                                            } catch (e: Exception) {
                                                Log.e("BITMAP_ERROR", "Failed to create bitmap from bytes", e)
                                                null
                                            }
                                        }

                                        bitmap?.let { validBitmap ->
                                            Image(
                                                bitmap = validBitmap.asImageBitmap(),
                                                contentDescription = "Customer ID",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                            )
                                            Text(
                                                "Image size: ${validBitmap.width}x${validBitmap.height}",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Control Number Input
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Control No.:",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFF8B4513),
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier.width(100.dp)
                                )
                                OutlinedTextField(
                                    value = controlNumber,
                                    onValueChange = { controlNumber = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .border(1.dp, Color(0xFF8B4513), RoundedCornerShape(8.dp)),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 14.sp,
                                        color = Color(0xFF5C4033)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        disabledContainerColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true
                                )
                            }

                            // Price Input and Calculation Display
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "₱",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = Color(0xFF8B4513),
                                            fontSize = 24.sp
                                        )
                                    )
                                    OutlinedTextField(
                                        value = priceInput,
                                        onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) priceInput = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 18.sp,
                                            color = Color(0xFF5C4033),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        placeholder = {
                                            Text(
                                                "0.00",
                                                color = Color.LightGray,
                                                fontSize = 16.sp
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFFE0C1A6).copy(alpha = 0.3f),
                                            unfocusedContainerColor = Color(0xFFE0C1A6).copy(alpha = 0.3f),
                                            disabledContainerColor = Color(0xFFE0C1A6).copy(alpha = 0.3f),
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Discounted Total: ₱${"%,.2f".format(priceValue)}",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color(0xFF006400),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .background(
                                                color = Color(0xFFE0C1A6).copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Buttons Row (Fixed Height)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)  // Fixed button row height
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
                            if (priceInput.isEmpty() || controlNumber.isEmpty()) {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            showConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                    ) {
                        Text(text = "CONFIRM", color = Color.White)
                    }
                }
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
                        idNumber = URLDecoder.decode(idNumberDb, "UTF-8"), // Decode original ID number
                        name = decodedName, // Use properly decoded name
                        city = decodedCity, // Use properly decoded city
                        citizenType = citizenType,
                        items = decodedItemsList,
                        date = currentDate,
                        time = currentTime,
                        cashierName = account.name,
                        branch = account.branch,
                        discountPercentage = discountPercentage.value,
                        controlNo = controlNumber,
                        originalPrice = priceInput.toFloatOrNull() ?: 0f,
                        totalPrice = priceValue,
                        customerID = customerID?.let {
                            try {
                                Base64.decode(it, Base64.DEFAULT)
                            } catch (e: Exception) {
                                null
                            }
                        }
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

    // Duplicate items alert
    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = {
                Text("Duplicate Food Items", 
                    fontWeight = FontWeight.Bold,
                    color = Color.Red)
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .sizeIn(maxHeight = 400.dp)
                ) {
                    Text("The following items have already been availed today:", 
                        fontWeight = FontWeight.SemiBold)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    duplicateFoodItems.forEach { item ->
                        Text("• $item", 
                            modifier = Modifier.padding(vertical = 2.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Existing Order Details:", 
                        fontWeight = FontWeight.SemiBold)
                    
                    existingOrderInfo?.forEach { (key, value) ->
                        Text("${key}: $value",
                            modifier = Modifier.padding(vertical = 2.dp),
                            fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDuplicateDialog = false
                        handleEditButtonClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDAA520))
                ) {
                    Text("Edit Order")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDuplicateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}