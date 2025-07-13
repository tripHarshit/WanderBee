package com.example.wanderbee.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wanderbee.R
import com.example.wanderbee.navigation.WanderBeeScreens

@Composable
fun BottomNavigationBar(
    selectedItem: String = "Home",
    onItemSelected: (String) -> Unit = {},
    navController: NavController
)  {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
        modifier = Modifier.height(85.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, color = Color.White.copy(alpha = .1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Home Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Home")
                        navController.navigate(WanderBeeScreens.HomeScreen.name)},
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Home") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Home",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Home") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            // Chat Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Chat")
                        navController.navigate(WanderBeeScreens.AllChatsScreen.name) },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Chat") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Chat",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Chat") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            // Events Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Events") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = "Event",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Events") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Events",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Events") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            // Saved Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Saved")
                        navController.navigate(WanderBeeScreens.SavedScreen.name)
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Saved",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Saved") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Saved",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Saved") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}