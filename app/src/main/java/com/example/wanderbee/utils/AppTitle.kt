package com.example.wanderbee.utils

import android.renderscript.ScriptGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wanderbee.R


@Composable
fun AppTitle(){
    Text(text = "WanderBee",
        fontSize = 32.sp,
        color = MaterialTheme.colorScheme.secondary,
        fontFamily = FontFamily(Font(R.font.coustard_regular)))
    Text(text = "Your Smart Travel Companion",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onTertiary,
        fontFamily = FontFamily(Font(R.font.istokweb_regular)))
}

