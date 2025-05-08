package com.example.wanderbee.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wanderbee.R


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HomeScreenDestinationsCard(
    city: String,
    place: String,
    imageUrl: String?,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
           // .padding(end = .dp)
            .height(110.dp)
            .width(150.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        when {
            isLoading -> {
                LoadingScreen(modifier = Modifier.matchParentSize())
            }

            imageUrl != null -> {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "location mark",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "$city, $place",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
