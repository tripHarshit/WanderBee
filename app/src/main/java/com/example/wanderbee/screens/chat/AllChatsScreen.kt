package com.example.wanderbee.screens.chat

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.wanderbee.utils.AllChatsScreenTopBar
import com.example.wanderbee.utils.BottomNavigationBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AllChatsScreen(
    navController: NavController
){
    var selectedTab by remember { mutableStateOf("Chat") }

    Scaffold(
        topBar = { AllChatsScreenTopBar(
            navController = navController
        ) },
        bottomBar = { BottomNavigationBar(
            selectedItem = selectedTab,
            onItemSelected = {tab -> selectedTab = tab},
            navController = navController
        ) }
    ) {

    }


}

@Composable
fun ChatScreenCard(){
    Card (modifier = Modifier.fillMaxSize()){

    }
}