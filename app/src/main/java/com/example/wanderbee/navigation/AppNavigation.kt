package com.example.wanderbee.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wanderbee.screens.home.HomeScreen
import com.example.wanderbee.screens.home.HomeScreenViewModel

@Composable
fun WanderBeeNavigation(){
    val navController = rememberNavController()
    val homeScreenViewModel: HomeScreenViewModel = viewModel()

    NavHost(navController = navController, startDestination = WanderBeeScreens.HomeScreen.name){
        composable(route = WanderBeeScreens.HomeScreen.name) {
            HomeScreen(navController = navController, viewModel = homeScreenViewModel)
        }
    }
}