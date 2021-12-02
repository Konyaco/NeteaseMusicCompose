package me.konyaco.neteasemusic

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.konyaco.neteasemusic.ui.App
import me.konyaco.neteasemusic.viewmodel.ViewModel

fun main() = application {
    val viewModel = remember { ViewModel(MusicPlayer()) }
    Window(
        onCloseRequest = ::exitApplication,
        title = "网易云音乐",
        state = rememberWindowState(width = 1030.dp, height = 680.dp),
        icon = painterResource("icon.png")
    ) {
        val density = LocalDensity.current
        val width = with(density) { 1030.dp.roundToPx() }
        val height = with(density) { 680.dp.roundToPx() }
        LaunchedEffect(width, height) {
//            window.minimumSize = Dimension(width, height)
        }
        App(viewModel)
    }
}
