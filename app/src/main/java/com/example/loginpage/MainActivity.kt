package com.example.loginpage

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
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val accountViewModel: AccountViewModel = viewModel()
            NavHost(
                navController = navController,
                startDestination = "Routes.LoginScreen",
                
                ){
                composable("Routes.LoginScreen") {
                    LoginScreen(navController)
                }
                composable("Routes.RegistrationScreen"){
                    RegistrationScreen(navController)
                }
                //NEW PIN INPUT SCREEN
                composable("Routes.PinInputScreen") {
                    PinInputScreen(navController, accountViewModel)
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
                        items = backStackEntry.arguments?.getString("items") ?: ""
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


                composable("Routes.ConfirmationScreen/{name}/{idNumber}/{city}/{items}") { backStackEntry ->
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    val idNumber = backStackEntry.arguments?.getString("idNumber") ?: ""
                    val items = backStackEntry.arguments?.getString("items") ?: ""
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    ConfirmationScreen(navController = navController, name = name, idNumber = idNumber, city = city, items = items)
                }
                composable(
                    "Routes.ManualScreen?selectedItems={selectedItems}",
                    arguments = listOf(
                        navArgument("selectedItems") {
                            type = NavType.StringType
                            defaultValue = ""
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val encodedItems = backStackEntry.arguments?.getString("selectedItems") ?: ""
                    val selectedItems = try {
                        URLDecoder.decode(encodedItems, "UTF-8").split(",").filter { it.isNotEmpty() }
                    } catch (e: Exception) {
                        emptyList<String>()
                    }

                    ManualScreen(
                        navController = navController,
                        accountViewModel = accountViewModel,
                        selectedItemsFromScanner = selectedItems
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
    }
}

