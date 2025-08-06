package com.example.wanderbee.screens.authentication

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wanderbee.R
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.utils.AppTitle
import com.example.wanderbee.utils.InputField
import com.example.wanderbee.utils.LoadingScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


@Composable
fun GetShowSignInScreen(navController: NavController, authViewModel: AuthViewModel = hiltViewModel()){
    val uiState by authViewModel.loginState.collectAsState()

    when(uiState){
        is State.Loading-> {
            LoadingScreen(modifier = Modifier)
        }

        State.Success -> {
            navController.navigate(WanderBeeScreens.HomeScreen.name){
                popUpTo(WanderBeeScreens.LoginScreen.name){
                    inclusive = true
                }
            }
        }
        State.Error -> {LoginScreen(navController,authViewModel,true)}
        State.Idle -> {LoginScreen(navController,authViewModel,false)}

    }

}

@Composable
fun LoginScreen(navController: NavController,authViewModel: AuthViewModel = hiltViewModel(),isError: Boolean){

    val context = LocalContext.current
    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("892713144798-1lh9uu643nl0bt65p423vmp2rb74hhn1.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleClient = GoogleSignIn.getClient(context, options)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            .result
        account?.idToken?.let { authViewModel.signInWithGoogle(it) }
    }

    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()

    Surface(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)) {

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            //logo
            Spacer(modifier = Modifier.height(120.dp))
            AppTitle()

            Spacer(modifier = Modifier.height(40.dp))

            //email
            InputField(value = email, label = "Email", onValueChanged = {
                authViewModel.updateEmail(it)
            })

            //password
            InputField(value = password, label = "Password", onValueChanged = {
                authViewModel.updatePass(it)
            })

            //forgot password
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()) {
                Text(text = "forgot password?", modifier = Modifier
                    .padding(start = 16.dp, bottom = 8.dp)
                    .clickable(onClick = {navController.navigate(WanderBeeScreens.ForgotPassword.name)}),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                    )
            }

            //login button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { authViewModel.signInWithEmailAndPassword(email,password) },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            //show caution if state is error
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
                        text = "Authorisation Revoked! Incorrect email or password",
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
                    text = "Or Login With",
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
                              popUpTo(WanderBeeScreens.LoginScreen.name){
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
                    text = "Don't have an account?",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = "SignUp",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable(onClick = {navController.navigate(WanderBeeScreens.SignUpScreen.name)})
                )
            }
        }
    }
}
