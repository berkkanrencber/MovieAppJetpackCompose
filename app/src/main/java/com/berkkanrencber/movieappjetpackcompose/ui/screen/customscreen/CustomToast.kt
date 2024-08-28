package com.berkkanrencber.movieappjetpackcompose.ui.screen.customscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import com.berkkanrencber.movieappjetpackcompose.R
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Black
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Charcoal
import com.berkkanrencber.movieappjetpackcompose.ui.theme.Gray
import com.berkkanrencber.movieappjetpackcompose.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun CustomToastComposable(
    message: String,
    iconResId: Int,
    durationMillis: Long = 2000L
) {
    Popup(alignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier
                .background(
                    color = Charcoal,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 2.dp,
                    color = White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(durationMillis)
    }
}

