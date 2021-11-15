package me.konyaco.neteasemusic.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?
) {
    Image(
        modifier = Modifier.size(24.dp),
        painter = painter,
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(Color.White)
    )
}