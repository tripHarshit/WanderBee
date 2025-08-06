package com.example.wanderbee.navigation

import com.example.wanderbee.screens.chat.ChatViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wanderbee.screens.authentication.ForgotPassword
import com.example.wanderbee.screens.authentication.GetShowSignInScreen
import com.example.wanderbee.screens.authentication.GetShowSignUpScreen
import com.example.wanderbee.screens.chat.AllChatsScreen
import com.example.wanderbee.screens.details.DetailsViewModel
import com.example.wanderbee.screens.details.InfoDetailsScreen
import com.example.wanderbee.screens.details.PhotosDetailsScreen
import com.example.wanderbee.screens.details.VideosDetailsScreen
import com.example.wanderbee.screens.home.HomeScreen
import com.example.wanderbee.screens.home.HomeScreenViewModel
import com.example.wanderbee.screens.itinerary.AiViewModel
import com.example.wanderbee.screens.itinerary.ItineraryDayScreen
import com.example.wanderbee.screens.itinerary.PlanItineraryScreen
import com.example.wanderbee.screens.chat.ChatScreen
import com.example.wanderbee.screens.chat.PrivateChatScreen
import com.example.wanderbee.screens.saved.SavedScreen
import com.example.wanderbee.screens.profile.ProfileScreen
import com.example.wanderbee.screens.splash.SplashScreen
import com.example.wanderbee.screens.splash.SplashViewModel
import com.example.wanderbee.screens.onboarding.OnboardingScreen

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun WanderBeeNavigation(){
    val navController = rememberNavController()
    val homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
    val detailsViewModel: DetailsViewModel = hiltViewModel()
    val aiViewModel: AiViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val splashViewModel: SplashViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = WanderBeeScreens.SplashScreen.name){
        
        // Splash Screen
        composable(route = WanderBeeScreens.SplashScreen.name) {
            SplashScreen(
                onNavigateToHome = { navController.navigate(WanderBeeScreens.HomeScreen.name) },
                onNavigateToOnboarding = { navController.navigate(WanderBeeScreens.OnboardingScreen.name) },
                isLoggedIn = splashViewModel.isUserLoggedIn(),
                isFirstLaunch = true // This will be managed by the ViewModel
            )
        }
        
        // Onboarding Screen
        composable(route = WanderBeeScreens.OnboardingScreen.name) {
            OnboardingScreen(
                onNavigateToLogin = { navController.navigate(WanderBeeScreens.LoginScreen.name) },
                onNavigateToHome = { navController.navigate(WanderBeeScreens.HomeScreen.name) },
                isLoggedIn = splashViewModel.isUserLoggedIn()
            )
        }
        
        composable(route = WanderBeeScreens.HomeScreen.name) {
           HomeScreen(navController = navController, homeScreenViewModel = homeScreenViewModel)
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
        composable(route = WanderBeeScreens.SavedScreen.name) {
            SavedScreen(navController = navController)
        }
        composable(route = WanderBeeScreens.ProfileScreen.name) {
            ProfileScreen(navController = navController)
        }
        composable(
            route = "${WanderBeeScreens.InfoDetailsScreen.name}/{city}/{dest}",
            arguments = listOf(
                navArgument(name = "city") {
                    type = NavType.StringType
                },
                navArgument(name = "dest") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            val dest = backStackEntry.arguments?.getString("dest") ?: ""
            InfoDetailsScreen(
                navController = navController,
                detailsViewModel = detailsViewModel,
                city = city,
                dest = dest,
                chatViewModel = chatViewModel
            )
        }

        composable(
            route = "${WanderBeeScreens.PhotosDetailsScreen.name}/{city}/{dest}",
            arguments = listOf(
                navArgument(name = "city") {
                    type = NavType.StringType
                },
                navArgument(name = "dest") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            val dest = backStackEntry.arguments?.getString("dest") ?: ""
            PhotosDetailsScreen(
                navController = navController,
                city = city,
                dest = dest,
                detailsViewModel = detailsViewModel
            )
        }

    composable(
        route = "${WanderBeeScreens.VideosDetailsScreen.name}/{city}/{dest}",
        arguments = listOf(
            navArgument(name = "city") {
                type = NavType.StringType
            },
            navArgument(name = "dest") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val city = backStackEntry.arguments?.getString("city") ?: ""
        val dest = backStackEntry.arguments?.getString("dest") ?: ""
        VideosDetailsScreen(
            navController = navController,
            city = city,
            dest = dest,
            detailsViewModel = detailsViewModel
        )
    }

            composable(
                route = "${WanderBeeScreens.PlanItineraryScreen.name}/{city}/{dest}",
                arguments = listOf(
                    navArgument(name = "city") {
                        type = NavType.StringType
                    },
                    navArgument(name = "dest") {
                        type = NavType.StringType
                    }
                )
            ) {backStackEntry->
                val city = backStackEntry.arguments?.getString("city") ?: ""
                val dest = backStackEntry.arguments?.getString("dest") ?: ""
               PlanItineraryScreen(
                   city = city,
                   dest = dest,
                   navController = navController
               )
            }
            composable("${WanderBeeScreens.ItineraryDayScreen.name}/{dayIndex}/{city}/{dest}",
                arguments = listOf(
                    navArgument(name = "dayIndex") {
                        type = NavType.IntType
                    },
                    navArgument(name = "city"){
                        type = NavType.StringType
                    },
                    navArgument(name = "dest"){
                        type = NavType.StringType
                    })
            ) { backStackEntry ->
                val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
                val city = backStackEntry.arguments?.getString("city") ?: ""
                val dest  = backStackEntry.arguments?.getString("dest") ?: ""
                ItineraryDayScreen(navController = navController, dest = dest, city = city, dayIndex = dayIndex)
            }

           composable(route = WanderBeeScreens.AllChatsScreen.name) {
               AllChatsScreen(navController = navController, chatViewModel)
           }

        composable(
            route = "GroupChat/{destinationId}/{destinationName}",
            arguments = listOf(
                navArgument(name = "destinationId") { type = NavType.StringType },
                navArgument(name = "destinationName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val destinationId = backStackEntry.arguments?.getString("destinationId") ?: ""
            val destinationName = backStackEntry.arguments?.getString("destinationName") ?: ""
            ChatScreen(
                navController = navController,
                destinationId = destinationId,
                destinationName = destinationName,
                chatViewModel = chatViewModel
            )
        }
        composable(
            route = "PrivateChat/{chatId}",
            arguments = listOf(
                navArgument(name = "chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            PrivateChatScreen(
                navController = navController,
                chatId = chatId,
                chatViewModel = chatViewModel
            )
        }
    }
}