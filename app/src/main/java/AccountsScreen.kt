package com.example.loginpage

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(navController: NavController, accountViewModel: AccountViewModel = viewModel()) {
    val accountHolder = accountViewModel.accountHolder.collectAsState().value
    val focusManager = LocalFocusManager.current

    val pin = remember { mutableStateOf("") }
    val context = LocalContext.current
    val discountPrefs = remember { DiscountPreferences(context) }
    val usernameInputAccount = remember { mutableStateOf("") }
    val seniorDiscount = remember { mutableStateOf(discountPrefs.getDiscountPercentage("senior").toString()) }
    val pwdDiscount = remember { mutableStateOf(discountPrefs.getDiscountPercentage("pwd").toString()) }
    val othersDiscount = remember { mutableStateOf(discountPrefs.getDiscountPercentage("others").toString()) }

    fun updateData(pin: String, cashierName: String, branch: String) {
        val url = "http://192.168.254.107/accounts.php"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getBoolean("success")) {
                    Toast.makeText(
                        context,
                        "Your account's PIN has been updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate("Routes.ScannerScreen") {
                        popUpTo("Routes.AccountsScreen") { inclusive = true }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Insert Failed: ${jsonResponse.getString("message")}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    context,
                    "Transaction Failed! Please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["pin"] = pin // Use 'otpText' (the entered PIN)

                accountHolder?.let { holder ->
                    params["cashierName"] = holder.name
                    params["branch"] = holder.branch
                } ?: run {
                    Log.e("PinAccountInputScreen", "accountHolder is null")
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
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.accounts), // logo
                    contentDescription = "Cafe Logo",
                    modifier = Modifier
                        .alpha(0.5f)
                        .height(60.dp)
                        .width(60.dp)
                        .padding(bottom = 10.dp)
                )

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
                        Text(
                            text = "UPDATE ACCOUNT",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFF8B4513), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Cashier Name: ${accountHolder?.name ?: "No User"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pin.value,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    pin.value = it
                                }
                            },
                            label = { Text("Enter New PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black  )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = usernameInputAccount.value,
                            onValueChange = {
                                if (it.matches(Regex("^[A-Za-z.,-]*$"))) {
                                    usernameInputAccount.value = it
                                }
                            },
                            label = { Text("Enter New Username") },
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

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        Text(
                            text = "UPDATE DISCOUNTS PERCENTAGES",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFF8B4513), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            @OptIn(ExperimentalMaterial3Api::class)
                            @Composable
                            fun DiscountField(
                                label: String,
                                valueState: MutableState<String>
                            ) {
                                OutlinedTextField(
                                    value = valueState.value,
                                    onValueChange = { newValue ->
                                        // Allow empty string or valid decimal numbers up to 2 decimal places
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*(\\.\\d{0,2})?$")) && newValue.length <= 5) {
                                            valueState.value = newValue
                                        }
                                    },
                                    label = { Text(text = label) },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color.Black,
                                        unfocusedBorderColor = Color.Black,
                                        focusedLabelColor = Color.Black,
                                        unfocusedLabelColor = Color.Black
                                    ),
                                    modifier = Modifier.width(100.dp)
                                )
                            }

                            DiscountField("Senior Citizen", seniorDiscount)
                            DiscountField("PWD", pwdDiscount)
                            DiscountField("Others", othersDiscount)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (pin.value.isNotEmpty()) {
                            Log.d("DEBUG", "updating account's PIN")
                            accountHolder?.let {
                                updateData(pin.value, it.name, it.branch)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please fill in all the fields",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Save discount percentages
                        seniorDiscount.value.toFloatOrNull()?.let {
                            discountPrefs.saveDiscountPercentage("senior", it)
                        }
                        pwdDiscount.value.toFloatOrNull()?.let {
                            discountPrefs.saveDiscountPercentage("pwd", it)
                        }
                        othersDiscount.value.toFloatOrNull()?.let {
                            discountPrefs.saveDiscountPercentage("others", it)
                        }

                        Toast.makeText(context, "Discount percentages updated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .bounceClick()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                ) {
                    Text(text = "SUBMIT", color = Color.White)
                }
            }
        }
    }
}

class DiscountPreferences(context: Context) {
    private val sharedPref = context.getSharedPreferences("discount_prefs", Context.MODE_PRIVATE)

    fun saveDiscountPercentage(type: String, percentage: Float) {
        with(sharedPref.edit()) {
            putFloat(type, percentage)
            apply()
        }
    }

    fun getDiscountPercentage(type: String): Float {
        return sharedPref.getFloat(type, 0f) // 0f is default value if not found
    }
}