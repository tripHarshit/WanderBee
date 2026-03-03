package com.example.wanderbee.screens.saved

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.wanderbee.R
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.utils.LoadingScreen
import com.example.wanderbee.utils.SavedScreenTopBar

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SavedScreen(
    navController: NavController,
    savedViewModel: SavedViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf("Saved") }
    val savedDestinations by savedViewModel.savedDestinations.collectAsState()

    LaunchedEffect(Unit) {
        savedViewModel.loadSavedDestinations()
    }

    Scaffold(
        topBar = { SavedScreenTopBar(navController = navController) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedTab,
                onItemSelected = { tab -> selectedTab = tab },
                navController = navController
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            if (savedDestinations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved destinations yet",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontFamily = FontFamily(Font(R.font.istokweb_regular))
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedDestinations) { savedDestination ->
                        LaunchedEffect(Unit) {
                            savedViewModel.savedCityCoverImage(savedDestination.city)
                        }
                        SavedScreenDestinationCard(
                            city = savedDestination.city,
                            destination = savedDestination.destination,
                            imageUrl = savedViewModel.savedImageUrl[savedDestination.city] ?: "",
                            onCardClick = {
                                navController.navigate("${WanderBeeScreens.InfoDetailsScreen.name}/${savedDestination.city}/${savedDestination.destination}")
                            },
                            onUnsaveClick = {
                                savedViewModel.unsaveDestination(savedDestination.destinationId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SavedScreenDestinationCard(
    city: String,
    destination: String,
    onCardClick: () -> Unit,
    imageUrl: String,
    onUnsaveClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(190.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onCardClick)
    ) {
        when {
            true -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "$city image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                4f, 4f, android.graphics.Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        },
                    alpha = .8f
                )
            }

            else -> {
                // Optionally show fallback UI
            }
        }


        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Unsave",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(65.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable(onClick = onUnsaveClick)
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "location mark",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$city,$destination",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                color = Color.White,
                fontSize = 20.sp,
                lineHeight = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
