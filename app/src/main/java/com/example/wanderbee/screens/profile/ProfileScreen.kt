package com.example.wanderbee.screens.profile

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.wanderbee.R
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.utils.ProfileScreenTopBar
import com.example.wanderbee.utils.rememberImagePicker
import com.example.wanderbee.navigation.WanderBeeScreens
import android.Manifest

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf("Profile") }
    val profileData by profileViewModel.profileData.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    // Image picker
    val imagePicker = rememberImagePicker { uri ->
        profileViewModel.uploadProfileImage(uri)
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            profileViewModel.requestLocationPermission()
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfileData()
    }

    Scaffold(
        topBar = { ProfileScreenTopBar(navController = navController) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedTab,
                onItemSelected = { tab -> selectedTab = tab },
                navController = navController
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Header Section
                    ProfileHeaderSection(
                        profileData = profileData,
                        onEditProfile = { /* TODO: Navigate to edit profile */ },
                        onImageClick = { imagePicker.launch("image/*") }
                    )

                    // Personal Information Section
                    PersonalInfoSection(
                        profileData = profileData,
                        profileViewModel = profileViewModel,
                        onLocationPermissionRequest = {
                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    )

                    // Travel Preferences Section
                    TravelPreferencesSection(
                        profileData = profileData,
                        profileViewModel = profileViewModel
                    )

                    // Account Actions Section
                    AccountActionsSection(
                        onLogout = {
                            profileViewModel.logout()
                            navController.navigate(WanderBeeScreens.LoginScreen.name) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    profileData: ProfileData,
    onEditProfile: () -> Unit,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .clickable(onClick = onImageClick),
                contentAlignment = Alignment.Center
            ) {
                if (profileData.profilePictureUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileData.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile Picture",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(60.dp)
                    )
                }

                // Add Photo Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = "Add Photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = profileData.name.ifEmpty { "Traveler" },
                fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Email
            if (profileData.email.isNotEmpty()) {
                Text(
                    text = profileData.email,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Button
            Button(
                onClick = onEditProfile,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Profile",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun PersonalInfoSection(
    profileData: ProfileData,
    profileViewModel: ProfileViewModel,
    onLocationPermissionRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Personal Information",
                fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            EditableInfoRow(
                icon = Icons.Outlined.Person,
                label = "Full Name",
                value = profileData.name.ifEmpty { "Not set" },
                onEdit = { newValue ->
                    profileViewModel.updateProfile(profileData.copy(name = newValue))
                }
            )

            EditableInfoRow(
                icon = Icons.Outlined.Email,
                label = "Email",
                value = profileData.email.ifEmpty { "Not set" },
                onEdit = { newValue ->
                    profileViewModel.updateProfile(profileData.copy(email = newValue))
                }
            )

            EditableInfoRow(
                icon = Icons.Outlined.Phone,
                label = "Phone",
                value = profileData.phone.ifEmpty { "Not set" },
                onEdit = { newValue ->
                    profileViewModel.updateProfile(profileData.copy(phone = newValue))
                }
            )

            EditableInfoRow(
                icon = Icons.Outlined.LocationOn,
                label = "Location",
                value = profileData.location.ifEmpty { "Not set" },
                onEdit = { newValue ->
                    profileViewModel.updateProfile(profileData.copy(location = newValue))
                },
                onLocationRequest = onLocationPermissionRequest
            )
        }
    }
}

@Composable
fun TravelPreferencesSection(
    profileData: ProfileData,
    profileViewModel: ProfileViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Travel Preferences",
                fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TravelStyleDropdownRow(
                icon = Icons.Outlined.TravelExplore,
                label = "Preferred Travel Style",
                value = profileData.travelStyle.ifEmpty { "Not set" },
                onSelection = { newValue ->
                    profileViewModel.updateProfile(profileData.copy(travelStyle = newValue))
                }
            )

            EditableInfoRow(
                icon = Icons.Outlined.Star,
                label = "Favorite Destinations",
                value = profileData.favoriteDestinations.ifEmpty { "Not set" },
                onEdit = { newValue ->
                    profileViewModel.updateProfile(profileData.copy(favoriteDestinations = newValue))
                }
            )
        }
    }
}

@Composable
fun AccountActionsSection(onLogout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Account Actions",
                fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Logout Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Logout",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}



@Composable
fun EditableInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onEdit: (String) -> Unit,
    onLocationRequest: (() -> Unit)? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            if (isEditing) {
                androidx.compose.material3.OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                )
            } else {
                Text(
                    text = value,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        if (isEditing) {
            Row {
                IconButton(
                    onClick = {
                        onEdit(editValue)
                        isEditing = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = {
                        editValue = value
                        isEditing = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Row {
                IconButton(
                    onClick = { isEditing = true }
                ) {
                    if (onLocationRequest != null && label != "Location") {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (onLocationRequest != null && label == "Location") {
                    IconButton(
                        onClick = onLocationRequest
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Get Current Location",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TravelStyleDropdownRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onSelection: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val travelStyles = listOf(
        "Adventure",
        "Relaxation", 
        "Cultural",
        "Budget",
        "Luxury",
        "Backpacking",
        "Family-friendly",
        "Solo Travel",
        "Business",
        "Romantic"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Box {
                Text(
                    text = value,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(vertical = 4.dp)
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    travelStyles.forEach { style ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = style,
                                    fontFamily = FontFamily(Font(R.font.istokweb_regular))
                                ) 
                            },
                            onClick = {
                                onSelection(style)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Select Travel Style",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(20.dp)
                .clickable { expanded = true }
        )
    }
} 