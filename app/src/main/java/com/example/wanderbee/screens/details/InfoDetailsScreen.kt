package com.example.wanderbee.screens.details

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wanderbee.utils.DetailsScreenTopBar
import com.example.wanderbee.R
import com.example.wanderbee.screens.home.BottomNavigationBar
import com.example.wanderbee.utils.SubHeading

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InfoDetailsScreen(navController: NavController,detailsViewModel: DetailsViewModel) {

    var selectedTab by remember { mutableStateOf("Home") }
    var selectedOption by remember { mutableStateOf("Info") }

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
                        .size(40.dp)
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
                    text = "Kyoto, Japan",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 35.sp
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
                    onClick = { selectedOption = "Info" },
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
                    onClick = { selectedOption = "Photos" },
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
                    onClick = { selectedOption = "Videos" },
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
            InfoScreenContent(detailsViewModel = detailsViewModel)
        }

    }
}

@Composable
fun InfoScreenContent(detailsViewModel: DetailsViewModel){
    val scrollState = rememberScrollState()

    // Collect the state
    val descriptionState = detailsViewModel.aiResponseState.collectAsState().value
    val culturalTipsState = detailsViewModel.culturalTipsState.collectAsState().value

    LaunchedEffect(key1 = "description") {
        detailsViewModel.getDescription("Kyoto")
        detailsViewModel.getCulturalTips("Kyoto")
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        when (descriptionState) {
            is AIResponseState.Loading -> {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is AIResponseState.Success -> {
                Text(
                    text = descriptionState.data,
                    color = MaterialTheme.colorScheme.onBackground,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            is AIResponseState.Error -> {
                Text(
                    text = "Error: ${descriptionState.message}",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            else -> {
                // Idle state or initial state
                Text(
                    text = "Loading description...",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
        }

        // Rest of your content
        Text(text = "Tags: [romantic, historic, popular]",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Text(text = "Currency: Yen",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Text(text = "Timezone: GMT+9",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )

        Text(text = "Language: Japanese",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        SubHeading(title = "Culture and Highlights")
        when (culturalTipsState) {
            is AIResponseState.Loading -> {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is AIResponseState.Success -> {
                Text(
                    text = culturalTipsState.data,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            is AIResponseState.Error -> {
                Text(
                    text = "Error: ${culturalTipsState.message}",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            else -> {
                Text(
                    text = "Loading description...",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
        }

        SubHeading(title = "Weather Conditions")
        Spacer(modifier = Modifier.height(10.dp))

    }
}
