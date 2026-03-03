package com.example.wanderbee.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wanderbee.R
import com.example.wanderbee.ui.theme.AppBackground
import com.example.wanderbee.ui.theme.Highlight
import com.example.wanderbee.ui.theme.InputField
import com.example.wanderbee.ui.theme.PrimaryButton
import com.example.wanderbee.ui.theme.Subheading

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val imageRes: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    isLoggedIn: Boolean
) {
    val pages = listOf(
        OnboardingPage(
            title = "Your Smart Travel Companion",
            description = "Discover amazing destinations and plan your perfect trip with AI-powered recommendations",
            imageUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&h=600&fit=crop"
        ),
        OnboardingPage(
            title = "AI-Powered Planning",
            description = "Get personalized itineraries, cultural tips, and local insights for every destination",
            imageUrl = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800&h=600&fit=crop"
        ),
        OnboardingPage(
            title = "Connect with Travelers",
            description = "Chat with fellow travelers, share experiences, and get real-time travel advice",
            imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"
        ),
        OnboardingPage(
            title = "Save & Organize",
            description = "Keep track of your favorite destinations and create your personal travel bucket list",
            imageUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800&h=600&fit=crop"
        )
    )
    
    val pagerState = androidx.compose.foundation.pager.rememberPagerState { pages.size }
    var currentPage by remember { mutableStateOf(0) }
    
    // Update current page when pager state changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { currentPage = it }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Skip button with better visibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Skip button - more prominent for logged-out users
                if (!isLoggedIn) {
                    TextButton(
                        onClick = { onNavigateToLogin() },
                        modifier = Modifier.align(Alignment.CenterEnd),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Subheading
                        )
                    ) {
                        Text(
                            text = "Skip to Login",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.istokweb_regular))
                        )
                    }
                } else {
                    TextButton(
                        onClick = { onNavigateToHome() },
                        modifier = Modifier.align(Alignment.CenterEnd),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Subheading
                        )
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.istokweb_regular))
                        )
                    }
                }
            }
            
            // Pager content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState
                ) { page ->
                    OnboardingPage(
                        page = pages[page],
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Bottom section with dots and button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        DotIndicator(
                            isSelected = index == currentPage,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Get Started button (only on last page)
                if (currentPage == pages.size - 1) {
                    Button(
                        onClick = {
                            if (isLoggedIn) onNavigateToHome() else onNavigateToLogin()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryButton
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = if (isLoggedIn) "Get Started" else "Login Now",
                            color = AppBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.istokweb_bold))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(InputField)
        ) {
            if (page.imageRes != null) {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = page.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = page.imageUrl,
                    contentDescription = page.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Highlight,
            fontFamily = FontFamily(Font(R.font.istokweb_bold)),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = Subheading,
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun DotIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 8.dp,
        animationSpec = tween(durationMillis = 300)
    )
    
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryButton else Subheading.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300)
    )
    
    Box(
        modifier = modifier
            .size(animatedSize)
            .background(
                color = animatedColor,
                shape = CircleShape
            )
    )
} 