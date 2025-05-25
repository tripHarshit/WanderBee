package com.example.wanderbee.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wanderbee.navigation.WanderBeeScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreenTopBar(navController: NavController){
    TopAppBar(title = {
        Row(horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton( onClick = {navController.navigate(WanderBeeScreens.HomeScreen.name)},
                content = {
                    Icon(imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp))
                })
        }
    })
}