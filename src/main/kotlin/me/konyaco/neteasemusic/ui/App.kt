package me.konyaco.neteasemusic.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.konyaco.neteasemusic.ui.page.LocalAndDownload
import me.konyaco.neteasemusic.ui.page.PlayPage
import me.konyaco.neteasemusic.viewmodel.ViewModel

@Composable
@Preview
fun App(viewModel: ViewModel) {
    MyTheme {
        var displayPlayPage by remember { mutableStateOf(false) }

        Column {
            Box(Modifier.weight(1f)) {
                Column {
                    TopBar()
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        SideNav(Modifier.fillMaxHeight().width(200.dp))
                        Divider(Modifier.fillMaxHeight().width(1.dp))
                        Body(Modifier.fillMaxHeight().weight(1f), viewModel)
                    }
                }
                PlayPage(viewModel, visible = displayPlayPage, onClose = { displayPlayPage = false })
            }
            PlayBar(viewModel, onAlbumImageClick = {
                displayPlayPage = true }
            )
        }
    }
}


@Composable
fun Body(modifier: Modifier, viewModel: ViewModel) {
    Box(modifier) {
        // TODO: 2021/11/14 Pages
        LocalAndDownload(viewModel)
    }
}