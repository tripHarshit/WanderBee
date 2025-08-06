package com.example.wanderbee.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wanderbee.R
import com.example.wanderbee.ui.theme.AppBackground
import com.example.wanderbee.ui.theme.Highlight
import com.example.wanderbee.ui.theme.InputField
import com.example.wanderbee.ui.theme.PrimaryButton
import com.example.wanderbee.ui.theme.Subheading
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    isLoggedIn: Boolean,
    isFirstLaunch: Boolean
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2000)
    )
    
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 2000, easing = EaseOutBack)
    )
    
    val slideAnim = animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 2000, easing = EaseOutCubic)
    )
    
    val logoRotation by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = EaseOutCubic)
    )
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000L)
        
        // Check if user is logged in first
        if (isLoggedIn) {
            // User is logged in, go directly to home screen
            onNavigateToHome()
        } else if (isFirstLaunch) {
            // First time user, show onboarding
            onNavigateToOnboarding()
        } else {
            // User has seen onboarding but not logged in, show onboarding again
            onNavigateToOnboarding()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Background decorative elements
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryButton.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .alpha(alphaAnim.value)
        )
        
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 300.dp, y = 600.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Highlight.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .alpha(alphaAnim.value)
        )
        
        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Logo container with card
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value),
                colors = CardDefaults.cardColors(
                    containerColor = InputField
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "WanderBee Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scaleAnim.value)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App name with enhanced styling
            Text(
                text = "WanderBee",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Highlight,
                fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .offset(y = slideAnim.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Slogan with enhanced styling
            Text(
                text = "Your Smart Travel Companion",
                fontSize = 18.sp,
                color = Subheading,
                fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle
            Text(
                text = "Discover • Plan • Explore",
                fontSize = 14.sp,
                color = Subheading.copy(alpha = 0.7f),
                fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
        
        // Enhanced loading indicator at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .alpha(alphaAnim.value)
        ) {
            EnhancedLoadingDots()
        }
        
        // Version or tag at bottom
        Text(
            text = "v1.0",
            fontSize = 12.sp,
            color = Subheading.copy(alpha = 0.5f),
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .alpha(alphaAnim.value)
        )
    }
}

@Composable
fun EnhancedLoadingDots() {
    val infiniteTransition = rememberInfiniteTransition()
    val dots = listOf(0, 1, 2)
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dots.forEach { index ->
            val delay = index * 200
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .background(
                        color = PrimaryButton.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
} 