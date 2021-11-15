package me.konyaco.neteasemusic.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopBar() {
    Row(
        Modifier.fillMaxWidth().height(60.dp).background(Color(0xffec4141)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(19.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource("icons/topbar/logo_white.svg"),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                contentScale = ContentScale.Inside
            )
            Spacer(Modifier.width(6.dp))
            Image(
                modifier = Modifier.size(93.dp, 24.dp),
                painter = painterResource("icons/topbar/logo_text.xml"),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(Modifier.width(87.dp))
            Box(Modifier.clip(CircleShape).size(24.dp).clickable { }.background(Color.Black.copy(0.08f))) {
                Image(
                    modifier = Modifier.align(Alignment.Center).size(16.dp),
                    painter = painterResource("icons/topbar/back.svg"),
                    contentDescription = "back",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.clip(CircleShape).size(24.dp).clickable { }.background(Color.Black.copy(0.08f))) {
                Image(
                    modifier = Modifier.align(Alignment.Center).size(16.dp),
                    painter = painterResource("icons/topbar/next.svg"),
                    contentDescription = "next",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(Modifier.clip(RoundedCornerShape(100.dp)).size(160.dp, 32.dp).background(Color.Black.copy(0.08f))) {
                Image(
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp).size(24.dp),
                    painter = painterResource("icons/topbar/search.svg"),
                    contentDescription = "search",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.clip(CircleShape).size(32.dp).clickable { }.background(Color.Black.copy(0.08f))) {
                Image(
                    modifier = Modifier.align(Alignment.Center).size(24.dp),
                    painter = painterResource("icons/topbar/listen.svg"),
                    contentDescription = "listen",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
        Spacer(Modifier.width(32.dp))
        // Action buttons
        Row(
            modifier = Modifier.fillMaxHeight().padding(end = 21.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserInfo()
            Icon(painterResource("icons/topbar/skin.svg"), "Skin")
            Icon(painterResource("icons/topbar/settings.svg"), "Settings")
            Icon(painterResource("icons/topbar/message.svg"), "Message")
            Box(Modifier.background(Color.White).size(1.dp, 16.dp))
            Icon(painterResource("icons/topbar/mini_mode.svg"), "Mini Mode")
            Icon(painterResource("icons/topbar/zoomout.svg"), "Minimum")
            Icon(painterResource("icons/topbar/zoomin.svg"), "Fullscreen")
            Icon(painterResource("icons/topbar/close.svg"), "Close")
        }
    }
}

@Composable
private fun UserInfo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Avatar
        Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(0.75f)))
        Spacer(Modifier.width(8.dp))
        // Name
        Text(text = "云音乐用户", fontSize = 13.sp, color = Color.White.copy(0.75f))
    }
}