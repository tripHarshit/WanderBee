package com.example.wanderbee.screens.authentication

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.ui.theme.WanderBeeTheme
import com.example.wanderbee.utils.AppTitle
import com.example.wanderbee.utils.InputField
import com.example.wanderbee.R
import androidx.compose.runtime.getValue
import com.example.wanderbee.utils.ErrorScreen
import com.example.wanderbee.utils.LoadingScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions



@Composable
fun GetShowSignUpScreen(navController: NavController, authViewModel: AuthViewModel = hiltViewModel()){
    val uiState by authViewModel.signUpState.collectAsState()

    when(uiState){
        is State.Loading-> {
            LoadingScreen()
        }

        State.Success -> {
            navController.navigate(WanderBeeScreens.HomeScreen.name){
                popUpTo(WanderBeeScreens.SignUpScreen.name){
                    inclusive = true
                }
            }
        }
        State.Error -> {
            SignUpScreen(navController,authViewModel,true)
        }
        State.Idle -> {SignUpScreen(navController,authViewModel,false)}
    }

}
@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthViewModel = hiltViewModel(),isError: Boolean){

    val context = LocalContext.current

    // Google Sign-In client setup
    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("892713144798-1lh9uu643nl0bt65p423vmp2rb74hhn1.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleClient = GoogleSignIn.getClient(context, options)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account = task.result
                account?.idToken?.let { authViewModel.signInWithGoogle(it) }
            } else {
                Log.e("SignUpScreen", "Google Sign-In failed", task.exception)
            }
        } catch (e: Exception) {
            Log.e("SignUpScreen", "Exception during Google Sign-In", e)
        }
    }

    val name by authViewModel.name.collectAsState()
    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()


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
            InputField(value = name, label = "Username", onValueChanged = {
                authViewModel.updateName(it)
            })

            //email
            InputField(value = email,label = "Email", onValueChanged = {
                authViewModel.updateEmail(it)
            })

            //password
            InputField(value = password,label = "Password", onValueChanged = {
                authViewModel.updatePass(it)
            })

            //caution
            if(password.isNotEmpty() && password.length < 8) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_error_24),
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Password contains at least 8 characters!",
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            //create account button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { authViewModel.signUpWithEmailAndPassword(email,password,name)},
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            //caution if fields are empty
            if(isError){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_error_24),
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "All fields must be filled!",
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
                onClick = { launcher.launch(googleClient.signInIntent)
                    navController.navigate(WanderBeeScreens.HomeScreen.name){
                        popUpTo(WanderBeeScreens.SignUpScreen.name){
                            inclusive = true
                        }
                    }},
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


