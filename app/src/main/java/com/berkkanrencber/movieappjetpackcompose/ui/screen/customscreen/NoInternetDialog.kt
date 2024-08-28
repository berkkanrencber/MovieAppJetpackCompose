package com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berkkanrencber.movieappjetpackcompose.R
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Charcoal
import com.berkkanrencber.movieappjetpackcompose.ui.theme.LightGray

@Composable
fun NoInternetDialog(
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        modifier = Modifier.padding(16.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightGray, shape = RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_no_internet),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.width(150.dp).height(100.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Internet Connection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please check your internet connection and try again.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRetry() },
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Retry",
                    color = Color.White
                )
            }
        }
    )
}


