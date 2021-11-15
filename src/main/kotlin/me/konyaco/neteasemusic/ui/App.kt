package me.konyaco.neteasemusic.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.konyaco.neteasemusic.ui.page.LocalAndDownload
import me.konyaco.neteasemusic.viewmodel.ViewModel

@Composable
@Preview
fun App(viewModel: ViewModel) {
    MyTheme {
        Column {
            TopBar()
            Row(Modifier.weight(1f).fillMaxWidth()) {
                SideNav(Modifier.fillMaxHeight().width(200.dp))
                Divider(Modifier.fillMaxHeight().width(1.dp))
                Body(Modifier.fillMaxHeight().weight(1f), viewModel)
            }
            PlayBar(viewModel)
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