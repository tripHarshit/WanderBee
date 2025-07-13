package com.example.wanderbee.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wanderbee.R
import com.example.wanderbee.navigation.WanderBeeScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreenTopBar(
    navController: NavController,
    isLiked: Boolean,
    city: String,
    dest: String,
    onLikeClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate(WanderBeeScreens.HomeScreen.name) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location Symbol",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.matchParentSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "$city, $dest",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 24.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onLikeClick,
                modifier = Modifier.padding(end = 10.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    )
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OtherScreensTopBar(navController: NavController){
    TopAppBar(title = {
        Row(horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton( onClick = {navController.popBackStack()},
                content = {
                    Icon(imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp))
                })
        }
    })
}

@Composable
fun ItineraryTopBar(navController: NavController, city: String, dest: String) {
    Column {
        Spacer(modifier = Modifier.height(60.dp)) // Optional top spacing

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Plan Itinerary",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "$city, $dest",
                        fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratedItineraryTopBar(navController: NavController, dest: String, onPrintClicked: () -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(60.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "$dest Itinerary",
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontSize = 24.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )

                IconButton(onClick = {  }) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircle,
                        contentDescription = "Save Itinerary",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onPrintClicked) {
                    Icon(
                        imageVector = Icons.Outlined.Print,
                        contentDescription = "Print Itinerary",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllChatsScreenTopBar(
    navController: NavController
) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate(WanderBeeScreens.HomeScreen.name) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

//                Box(
//                    modifier = Modifier
//                        .size(36.dp)
//                        .padding(start = 8.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.ChatBubble,
//                        contentDescription = "Location Symbol",
//                        tint = MaterialTheme.colorScheme.secondary,
//                        modifier = Modifier.matchParentSize()
//                    )
//                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Travel Chats",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 24.sp
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InnerChatScreenTopBar(
    navController: NavController,
    heading: String
) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate(WanderBeeScreens.AllChatsScreen.name) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = heading,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 24.sp
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreenTopBar(
    navController: NavController
) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate(WanderBeeScreens.HomeScreen.name) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back Arrow",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Saved Destinations",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 24.sp
                )
            }
        }
    )
}