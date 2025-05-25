package com.example.wanderbee.screens.details

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wanderbee.R
import com.example.wanderbee.data.remote.models.media.PexelsVideo
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.screens.home.BottomNavigationBar
import com.example.wanderbee.utils.DetailsScreenTopBar

@Composable
fun VideosDetailsScreen(navController: NavController, city: String, dest: String){

        var selectedTab by remember { mutableStateOf("") }
        var selectedOption by remember { mutableStateOf("Videos") }
        Log.d("selectedOption", selectedOption)
        Scaffold(
            topBar = { DetailsScreenTopBar(navController = navController) },
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
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                Spacer(modifier = Modifier.height(8.dp))

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
                PexelsVideoGallery(query = city)
            }

        }
    }

@Composable
fun PexelsVideoGallery(
    viewModel: DetailsViewModel = hiltViewModel(),
    query: String,
) {
    LaunchedEffect(key1 = query) {
        viewModel.searchVideos(query)
    }

    val videosState = viewModel.videosState.collectAsState().value

    when (videosState) {
        is PexelsVideoUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PexelsVideoUiState.Success -> {
            LazyColumn(
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videosState.videos,
                    key = {it.id}) { video ->
                    VideoItem(video)
                }
            }
        }

        is PexelsVideoUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${videosState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoItem(video: PexelsVideo) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }

    // Find the best quality video file (HD or SD)
    val videoFile = video.video_files.firstOrNull {
        it.quality == "hd" && it.width <= 1280
    } ?: video.video_files.firstOrNull {
        it.quality == "sd"
    } ?: video.video_files.firstOrNull()

    if (videoFile == null) return

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false
            setMediaItem(MediaItem.fromUri(videoFile.link))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp,end = 8.dp, bottom = 8.dp)
            .height(220.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        border = BorderStroke(0.5.dp, color = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()) {
            if (isPlaying) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Video thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                    IconButton(
                        onClick = {
                            isPlaying = true
                            exoPlayer.play()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }


