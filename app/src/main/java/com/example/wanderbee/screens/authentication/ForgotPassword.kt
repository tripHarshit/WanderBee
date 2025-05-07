package com.example.wanderbee.screens.authentication

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wanderbee.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.utils.ErrorScreen
import com.example.wanderbee.utils.LoadingScreen

@Composable
fun ForgotPassword(navController: NavController, authViewModel: AuthViewModel = hiltViewModel()) {
    val uiState by authViewModel.forgotState.collectAsState()
    val email = remember { mutableStateOf("") }

    if (uiState == State.Loading) {
        LoadingScreen(modifier = Modifier)
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            Text(
                text = "Reset Password",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily(Font(R.font.coustard_regular))
            )

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(text = "Recovery Email") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            Button(
                onClick = {
                    authViewModel.sendPasswordResetEmail(email.value)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                enabled = uiState != State.Loading
            ) {
                Text(text = "Send")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is State.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_check_circle_outline_24),
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Email sent successfully!",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1500)
                        navController.navigate(WanderBeeScreens.LoginScreen.name)
                    }
                }

                is State.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_error_24),
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Email doesn't exist! Try Again",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {}
            }
        }
    }
}
