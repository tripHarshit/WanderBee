package com.example.wanderbee.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wanderbee.screens.authentication.ForgotPassword
import com.example.wanderbee.screens.authentication.GetShowSignInScreen
import com.example.wanderbee.screens.authentication.GetShowSignUpScreen
import com.example.wanderbee.screens.authentication.LoginScreen
import com.example.wanderbee.screens.authentication.SignUpScreen
import com.example.wanderbee.screens.home.HomeScreen
import com.example.wanderbee.screens.home.HomeScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun WanderBeeNavigation(){
    val navController = rememberNavController()
    val homeScreenViewModel: HomeScreenViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = WanderBeeScreens.HomeScreen.name){
        composable(route = WanderBeeScreens.HomeScreen.name) {
           HomeScreen(navController = navController, homeScreenViewModel = homeScreenViewModel)
            //  HomeScreen()
        }
        composable(route = WanderBeeScreens.SignUpScreen.name) {
            GetShowSignUpScreen(navController = navController)
        }
        composable(route = WanderBeeScreens.LoginScreen.name) {
            GetShowSignInScreen(navController = navController)
        }
        composable(route = WanderBeeScreens.ForgotPassword.name) {
            ForgotPassword(navController = navController)
        }
    }
}