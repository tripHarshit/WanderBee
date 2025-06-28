package com.example.wanderbee.screens.details

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wanderbee.R
import com.example.wanderbee.data.remote.models.media.PexelsPhoto
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.utils.DetailsScreenTopBar
import kotlin.random.Random

@Composable
fun PhotosDetailsScreen(navController: NavController, city: String, dest: String,detailsViewModel: DetailsViewModel){

        var selectedTab by remember { mutableStateOf("") }
        var selectedOption by remember { mutableStateOf("Photos") }
    var isLiked by remember { mutableStateOf(false) }
    Log.d("selectedOption", selectedOption)
        Scaffold(
            topBar = {DetailsScreenTopBar(navController = navController,
                isLiked = detailsViewModel.isLiked,
                onLikeClick = { detailsViewModel.toggleLike() },
                city = city,
                dest = dest)},
            bottomBar =
                {
                    BottomNavigationBar(
                        selectedItem = selectedTab,
                        onItemSelected = { tab -> selectedTab = tab },
                        navController = navController
                    )
                }) {
            Column(
                modifier = Modifier.padding(paddingValues = it),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
               Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { selectedOption = "Info"
                            navController.navigate("${WanderBeeScreens.InfoDetailsScreen.name}/$city/$dest")},
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                        border = BorderStroke(
                            0.5.dp,
                            color = if (selectedOption == "Info") {
                                MaterialTheme.colorScheme.secondary
                            } else MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            text = "Info",
                            color = if (selectedOption == "Info") {
                                MaterialTheme.colorScheme.secondary
                            } else MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.istokweb_regular))
                        )
                    }

                    Button(
                        onClick = { selectedOption = "Photos"
                            navController.navigate("${WanderBeeScreens.PhotosDetailsScreen.name}/$city/$dest")},
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                        border = BorderStroke(
                            0.5.dp,
                            color = if (selectedOption == "Photos") {
                                MaterialTheme.colorScheme.secondary
                            } else MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            text = "Photos",
                            color = if (selectedOption == "Photos") {
                                MaterialTheme.colorScheme.secondary
                            } else MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.istokweb_regular))
                        )
                    }

                    Button(
                        onClick = { selectedOption = "Videos"
                            navController.navigate("${WanderBeeScreens.VideosDetailsScreen.name}/$city/$dest")},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                        border = BorderStroke(
                            0.5.dp,
                            color = if (selectedOption == "Videos") {
                                MaterialTheme.colorScheme.secondary
                            } else MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            text = "Videos",
                            color = if (selectedOption == "Videos") {
                                MaterialTheme.colorScheme.secondary
                            } else MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.istokweb_regular))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                PexelsGallery(query = city )
            }

        }
}

@Composable
fun PexelsGallery(
    viewModel: DetailsViewModel = hiltViewModel(),
    query: String
) {

    LaunchedEffect(key1 = query) {
        viewModel.searchPhotos(query)
    }

    val photosState = viewModel.photosState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        when (photosState) {
            is PexelsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PexelsUiState.Success -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photosState.photos, key = {it.id}) { photo ->
                        PhotoItem(photo)
                    }
                }
            }

            is PexelsUiState.Error-> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${photosState.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
fun PhotoItem(photo: PexelsPhoto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(width = .5.dp, color = MaterialTheme.colorScheme.secondary)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.src.medium)
                .crossfade(true)
                .build(),
            contentDescription = "Photo ${photo.id}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    ratio = Random.nextFloat().coerceIn(0.7f, 1.3f),
                    matchHeightConstraintsFirst = true
                )
        )
    }
}
