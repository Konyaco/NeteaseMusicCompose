package me.konyaco.neteasemusic.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.konyaco.neteasemusic.ui.page.LocalAndDownload
import me.konyaco.neteasemusic.ui.page.PlayPage
import me.konyaco.neteasemusic.viewmodel.LocalAndDownloadViewModel
import me.konyaco.neteasemusic.viewmodel.PlayViewModel
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
                        Body(Modifier.fillMaxHeight().weight(1f), viewModel as LocalAndDownloadViewModel)
                    }
                }
                PlayPage(viewModel as PlayViewModel, visible = displayPlayPage, onClose = { displayPlayPage = false })
            }
            PlayBar(viewModel as PlayViewModel, onAlbumImageClick = {
                displayPlayPage = true
            }
            )
        }
    }
}


@Composable
fun Body(modifier: Modifier, viewModel: LocalAndDownloadViewModel) {
    Box(modifier) {
        // TODO: 2021/11/14 Pages
        LocalAndDownload(viewModel)
    }
}