package com.example.wanderbee.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wanderbee.R

@Composable
fun TripSummaryCard(){

    Card(modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(Color.DarkGray),
        elevation = CardDefaults.cardElevation(8.dp)){

        Row(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
            horizontalArrangement = Arrangement.Start) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .width(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.japan),
                    contentDescription = "google image",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(250.dp)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {

                    Text(text = "Kyoto, Japan",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 1)

                    Spacer(modifier = Modifier.weight(1f))

                    Text(text = "14 days left",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 8.dp,end= 8.dp),
                        maxLines = 1)
                }

                Row(horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {

                    Icon(imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Calendar Icon",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                        modifier = Modifier.size(20.dp).padding(end= 8.dp, bottom = 8.dp)
                    )

                    Text(text = "May 15 - May 25, 2025",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 1)
                }

                Row(horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {

                    Text(text = "Trip Planning",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 10.sp,
                        modifier = Modifier,
                        maxLines = 1)

                    Spacer(modifier = Modifier.weight(1f))

                    Text(text = "80% completed",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 8.dp),
                        maxLines = 1)
                }
                CustomLinearProgressBar(.8f)
            }

        }
    }
}