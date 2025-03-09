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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
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
                    PinInputScreen(navController)
                }
                composable("Routes.PinAccountInputScreen"){
                    PinAccountInputScreen(navController)
                }

                composable(
                    "confirmation_screen/{name}/{idNumber}/{city}",
                    arguments = listOf(
                        navArgument("name") { type = NavType.StringType },
                        navArgument("idNumber") { type = NavType.StringType },
                        navArgument("city") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    ConfirmationScreen(
                        navController = navController,
                        name = backStackEntry.arguments?.getString("name") ?: "",
                        idNumber = backStackEntry.arguments?.getString("idNumber") ?: "",
                        city = backStackEntry.arguments?.getString("city") ?: ""
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
                    ScannerScreen(navController = navController)
                }

                //nav host para mapasa yung data from manual screen to confirmation screen
                /*
                composable("Routes.ConfirmationScreen/{name}/{idNumber}/{pwd}") { backStackEntry ->
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    val idNumber = backStackEntry.arguments?.getString("idNumber") ?: ""
                    val pwd = backStackEntry.arguments?.getString("pwd") ?: ""
                    ConfirmationScreen(navController = navController, name = name, idNumber = idNumber, pwd = pwd)
                }
                composable("Routes.ManualScreen"){
                    ManualScreen(navController)
                }
                */
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
                AccountsScreen(navController = navController)
            }
            }
        }
    }
}

