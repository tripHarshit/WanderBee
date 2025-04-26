package com.example.wanderbee.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wanderbee.screens.authentication.LoginScreen
import com.example.wanderbee.screens.authentication.SignUpScreen
import com.example.wanderbee.screens.home.HomeScreen
import com.example.wanderbee.screens.home.HomeScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun WanderBeeNavigation(){
    val navController = rememberNavController()
    val homeScreenViewModel: HomeScreenViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = WanderBeeScreens.SignUpScreen.name){
        composable(route = WanderBeeScreens.HomeScreen.name) {
            HomeScreen(navController = navController, viewModel = homeScreenViewModel)
        }
        composable(route = WanderBeeScreens.SignUpScreen.name) {
            SignUpScreen(navController = navController)
        }
        composable(route = WanderBeeScreens.LoginScreen.name) {
            LoginScreen(navController = navController)
        }
    }
}