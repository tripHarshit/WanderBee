package com.example.wanderbee.screens.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.ui.theme.WanderBeeTheme
import com.example.wanderbee.utils.AppTitle
import com.example.wanderbee.utils.InputField
import com.example.wanderbee.R

@Composable
fun SignUpScreen(navController: NavController){

    val name = rememberSaveable { mutableStateOf("") }
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }


    Surface(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)){

        Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {

            //logo
            Spacer(modifier = Modifier.height(120.dp))
            AppTitle()

            Spacer(modifier = Modifier.height(40.dp))

            //name
            InputField(value = name.value,label = "Username", onValueChanged = {})

            //email
            InputField(value = email.value,label = "Email", onValueChanged = {})

            //password
            InputField(value = password.value,label = "Password", onValueChanged = {})

            //create account button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { navController.navigate(WanderBeeScreens.HomeScreen.name) },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
             Spacer(modifier = Modifier.height(30.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp),
                    color = Color.Gray
                )
                Text(
                    text = "Or Continue With",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            //google auth
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = {  },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier
                        .padding(end = 16.dp)
                        .size(30.dp)){
                        Image(painter = painterResource(R.drawable.google),
                            contentDescription = "google logo")
                    }
                    Text(
                        text = "Google",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }

             Row(verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.Center,
                 modifier = Modifier.padding(top = 16.dp)){
                 Text(
                     text = "Already have an account?",
                     color = MaterialTheme.colorScheme.onBackground,
                     modifier = Modifier.padding(end = 8.dp)
                 )

                 Text(
                     text = "SignIn",
                     fontSize = 17.sp,
                     fontWeight = FontWeight.Bold,
                     color = MaterialTheme.colorScheme.secondary,
                     modifier = Modifier.clickable(onClick = {navController.navigate(WanderBeeScreens.LoginScreen.name)}))
             }
        }

    }
}

