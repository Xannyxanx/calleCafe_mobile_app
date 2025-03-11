import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.loginpage.EmptyTopAppBar
import com.example.loginpage.OtpTextField
import com.example.loginpage.R
import com.example.loginpage.bounceClick
import org.json.JSONObject
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginpage.AccountHolder
import com.example.loginpage.AccountViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PinInputScreen(navController: NavController, accountViewModel: AccountViewModel = viewModel()) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var otpText by remember { mutableStateOf("") }
    var loginSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            navController.navigate("Routes.ScannerScreen") {
                popUpTo("Routes.LoginScreen") { inclusive = true }
            }
        }
    }

    fun selectData(pin: String) {
        val url = "http://192.168.254.107/CalleCafe/login.php"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val cashierName = jsonResponse.getJSONObject("user").getString("name")
                        val branch = jsonResponse.getJSONObject("user").getString("branch")
                        accountViewModel.setAccount(AccountHolder(name = cashierName, branch = branch))
                        loginSuccess = true
                    } else {
                        errorMessage = jsonResponse.getString("message")
                    }
                } catch (e: Exception) {
                    errorMessage = "Unexpected response from server"
                }
            },
            Response.ErrorListener {
                errorMessage = "Login Failed! Please check your internet connection"
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("pin" to pin)
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
                EmptyTopAppBar(navController = navController)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF5C4033))
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Icon(
                    painter = painterResource(id = R.drawable.password),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .alpha(0.5f)
                        .height(100.dp)
                        .width(100.dp)
                        .padding(bottom = 16.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ENTER PIN",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(Color(0xFF8B4513), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OtpTextField(
                            otpText = otpText,
                            onValueChange = { otpText = it }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                Button(
                    onClick = {
                        if (otpText.isNotEmpty()) {
                            selectData(otpText)
                        } else {
                            Toast.makeText(context, "Please enter your PIN", Toast.LENGTH_SHORT).show()
                        }
                        focusManager.clearFocus()
                        if (otpText.length < 4) {
                            Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .bounceClick()
                        .height(64.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                ) {
                    Text(text = "SUBMIT", color = Color.White)
                }

                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    errorMessage = null
                }
            }
        }
    }
}