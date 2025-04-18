package com.example.loginpage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import DiscountManager
import PinAccountInputScreen
import PinInputScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var discountManager: DiscountManager
    private var idleTimer by mutableStateOf(System.currentTimeMillis())

    companion object {
        const val TIMEOUT_DURATION = 180_000L // 30 seconds for testing
    }

    fun resetTimer() {
        idleTimer = System.currentTimeMillis()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        discountManager = DiscountManager(this)
        
        CoroutineScope(Dispatchers.IO).launch {
            discountManager.fetchDiscounts()
        }

        setContent {
            val navController = rememberNavController()
            val accountViewModel: AccountViewModel = viewModel()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { _ ->
                            resetTimer()
                        }
                    }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "Routes.LoginScreen"
                ) {
                    composable("Routes.LoginScreen") {
                        LoginScreen(navController)
                    }
                    composable("Routes.RegistrationScreen"){
                        RegistrationScreen(navController)
                    }
                    //NEW PIN INPUT SCREEN
                    composable("Routes.PinInputScreen/{username}") { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        PinInputScreen(navController, username, accountViewModel)
                    }
                    composable("Routes.PinAccountInputScreen"){
                        PinAccountInputScreen(navController, accountViewModel)
                    }
                    composable("ManualScreen") {
                        ManualScreen(navController, accountViewModel)
                    }


                    composable(
                        "confirmation_screen/{name}/{idNumber}/{city}/{items}",
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("idNumber") { type = NavType.StringType },
                            navArgument("city") { type = NavType.StringType },
                            navArgument("items") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        ConfirmationScreen(
                            navController = navController,
                            name = backStackEntry.arguments?.getString("name") ?: "",
                            idNumber = backStackEntry.arguments?.getString("idNumber") ?: "",
                            city = backStackEntry.arguments?.getString("city") ?: "",
                            items = backStackEntry.arguments?.getString("items") ?: "",
                            discountManager = discountManager,
                            accountViewModel = accountViewModel // Make sure this is passed
                        )
                    }

                    //Nav host para mapasa yung data from login screen
                    composable(
                        route = "Routes.ScannerScreen?cashierName={cashierName}",
                        arguments = listOf(
                            navArgument("cashierName") {
                                type = NavType.StringType
                                defaultValue = "No user"
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->
                        ScannerScreen(navController, accountViewModel)
                    }


                    composable(
                        "Routes.ConfirmationScreen/{name}/{idNumber}/{city}/{items}",
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("idNumber") { type = NavType.StringType },
                            navArgument("city") { type = NavType.StringType },
                            navArgument("items") { 
                                type = NavType.StringType 
                                nullable = true // Make items nullable
                                discountManager = discountManager
                            }

                        )
                    ) { backStackEntry ->
                        // Add customer ID extraction
                        val itemsParam = backStackEntry.arguments?.getString("items") ?: ""
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val idNumber = backStackEntry.arguments?.getString("idNumber") ?: ""
                        val city = backStackEntry.arguments?.getString("city") ?: ""
                        ConfirmationScreen(
                            navController = navController, 
                            name = name, 
                            idNumber = idNumber, 
                            city = city, 
                            items = itemsParam, 
                            discountManager = discountManager,
                            accountViewModel = accountViewModel
                        )
                    }
                    composable(
                        "Routes.ManualScreen?selectedItems={selectedItems}&prefilled={prefilled}",
                        arguments = listOf(
                            navArgument("selectedItems") {
                                type = NavType.StringType
                                defaultValue = ""
                                nullable = true
                            },
                            navArgument("prefilled") {
                                type = NavType.StringType
                                defaultValue = ""
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->
                        val encodedItems = backStackEntry.arguments?.getString("selectedItems") ?: ""
                        val prefilled = backStackEntry.arguments?.getString("prefilled") ?: ""
                        
                        val selectedItems = try {
                            URLDecoder.decode(encodedItems, "UTF-8").split(",").filter { it.isNotEmpty() }
                        } catch (e: Exception) {
                            emptyList<String>()
                        }

                        ManualScreen(
                            navController = navController,
                            accountViewModel = accountViewModel,
                            selectedItemsFromScanner = selectedItems,
                            prefilled = prefilled
                        )
                    }

                    //navhost para mapasa name ni cashier to account settings
                    composable (
                        route = "Routes.AccountsScreen?cashierName={cashierName}",
                    arguments = listOf(
                        navArgument("cashierName") {
                            type = NavType.StringType
                            defaultValue = "No user"
                            nullable = true
                        }
                    )
                    ) { backStackEntry ->
                        AccountsScreen(navController, accountViewModel)
                    }
                }
            }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - idleTimer > TIMEOUT_DURATION) {
                        navController.navigate("Routes.LoginScreen") {
                            popUpTo(0)
                        }
                        resetTimer()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resetTimer()
    }
}