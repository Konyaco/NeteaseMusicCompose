package me.konyaco.neteasemusic.ui.page

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import me.konyaco.neteasemusic.ui.sizeToText
import me.konyaco.neteasemusic.ui.timeStampToText
import me.konyaco.neteasemusic.viewmodel.ViewModel
import java.awt.Cursor

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun LocalAndDownload(viewModel: ViewModel) {
    val scrollState = rememberScrollState()
    val songs by viewModel.localSongList.collectAsState()
    val location by viewModel.localSongDirectory.collectAsState()
    var changeDirectoryDialog by remember { mutableStateOf(false) }
    val isRefreshing by viewModel.isLocalSongListRefreshing.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshSongList() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 32.dp, vertical = 16.dp)) {
            var selected by remember { mutableStateOf(1) }
            // Tab
            Row {
                PrimaryTab("下载管理", selected == 0) { selected = 0 }
                Spacer(Modifier.width(16.dp))
                PrimaryTab("本地音乐", selected == 1) { selected = 1 }
            }
            Spacer(Modifier.height(12.dp))
            Row {
                Text(modifier = Modifier.weight(1f), text = "本地共有${songs.size}首歌曲 目录：$location", fontSize = 14.sp)
                Text(modifier = Modifier.clickable {
                    changeDirectoryDialog = true
                }, text = "选择目录", color = Color(0xFF0B58C1), fontSize = 14.sp)
            }
            Spacer(Modifier.height(32.dp))
            // Play all songs button & Refresh button
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayButton(onAddClick = {
                    viewModel.replacePlayList()
                }, onPlayClick = {
                    viewModel.replacePlayListAndPlay()
                })
                Spacer(Modifier.width(12.dp))
                RefreshButton { viewModel.refreshSongList() }
                Spacer(Modifier.width(12.dp))
                if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            Spacer(Modifier.height(16.dp))
            // Songs list
            SongList(songs = songs, onSongSelect = { viewModel.selectSong(it) })
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
            adapter = rememberScrollbarAdapter(scrollState)
        )

        if (changeDirectoryDialog) ChangeDirectoryDialog(onDismissRequest = {
            changeDirectoryDialog = false
        }, onConfirm = {
            viewModel.changeDirectory(it)
            changeDirectoryDialog = false
        })
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PrimaryTab(
    label: String,
    selected: Boolean,
    onSelectedStateChange: (Boolean) -> Unit
) {
    Box(
        Modifier.height(42.dp).width(IntrinsicSize.Max)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onSelectedStateChange(!selected) }
            )
            .pointerHoverIcon(remember { PointerIcon(Cursor(Cursor.HAND_CURSOR)) })
    ) {
        // Label
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = label,
            fontSize = if (selected) 19.sp else 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        // Indicator
        if (selected) Box(
            Modifier.fillMaxWidth().height(3.dp).padding(horizontal = 8.dp).align(Alignment.BottomCenter)
                .background(Color(0xFFEC4141))
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayButton(
    onPlayClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.height(32.dp).wrapContentWidth().clip(RoundedCornerShape(50)).background(Color(0xFFEC4141)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Button
        Row(Modifier.clickable { onPlayClick() }
            .pointerHoverIcon(remember { PointerIcon(Cursor(Cursor.HAND_CURSOR)) })
            .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource("icons/page/download/btn_play.svg"),
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = "Play all"
            )
            Spacer(Modifier.width(4.dp))
            Text(text = "播放全部", color = Color.White, fontSize = 14.sp)
        }
        // Divider
        Box(Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(0.12f)))
        // Right Button
        Box(Modifier.fillMaxHeight().clickable { onAddClick() }
            .pointerHoverIcon(remember { PointerIcon(Cursor(Cursor.HAND_CURSOR)) })
            .padding(horizontal = 12.dp)
        ) {
            Image(
                modifier = Modifier.size(18.dp).align(Alignment.Center),
                painter = painterResource("icons/page/download/btn_add.svg"),
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = "Add to song list"
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RefreshButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier.height(32.dp)
            .wrapContentWidth()
            .border(Dp.Hairline, Color(0xFFD8D8D8), RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .pointerHoverIcon(remember { PointerIcon(Cursor(Cursor.HAND_CURSOR)) })
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource("icons/page/download/btn_scan.svg"),
            colorFilter = ColorFilter.tint(Color.Black),
            contentDescription = "Refresh musics"
        )
        Spacer(Modifier.width(4.dp))
        Text(text = "刷新音乐", color = Color.Black, fontSize = 14.sp)
    }
}

@Composable
private fun SongList(songs: List<ViewModel.LocalSongInfo>, onSongSelect: (ViewModel.LocalSongInfo) -> Unit) {
    // TODO: 2021/11/14 Resizable column width
    Column {
        Header()
        songs.forEachIndexed { index, item ->
            Item(
                index + 1,
                item.name,
                item.author,
                item.album,
                timeStampToText(item.totalDurationMillis),
                sizeToText(item.size),
                onDoubleClick = { onSongSelect(item) }
            )
        }
    }
}

@Composable
private fun Table(
    content: TableScope.() -> Unit
) {

}

interface TableScope {
    fun header(
        vararg content: @Composable () -> Unit
    ) {

    }

    fun item(vararg content: @Composable () -> Unit) {

    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(64.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = "音乐标题",
            color = Color.Black.copy(0.5f),
            fontSize = 13.sp
        )
        remember { arrayOf("歌手", "专辑", "时长", "大小") }.forEach {
            Text(
                modifier = Modifier.width(88.dp),
                text = it,
                color = Color.Black.copy(0.5f),
                fontSize = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Item(
    index: Int,
    name: String,
    author: String,
    album: String,
    duration: String,
    size: String,
    onDoubleClick: () -> Unit
) {
    Row(
        modifier = Modifier.height(36.dp)
            .combinedClickable(onClick = {}, onDoubleClick = onDoubleClick)
            .background(if (index % 2 == 0) Color.Transparent else Color(0x05000000)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.width(64.dp).padding(end = 18.dp),
            text = remember(index) { index.toString() },
            textAlign = TextAlign.End,
            color = Color.Black.copy(0.3f),
            fontSize = 13.sp
        )
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            color = Color.Black.copy(0.75f),
            fontSize = 13.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        arrayOf(author, album, duration, size).forEach {
            Text(
                modifier = Modifier.width(88.dp),
                text = it,
                color = Color.Black.copy(0.5f),
                fontSize = 13.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ChangeDirectoryDialog(onDismissRequest: () -> Unit, onConfirm: (String) -> Unit) {
    Dialog(
        onCloseRequest = onDismissRequest,
        title = "更改目录",
        resizable = false,
        state = rememberDialogState(size = DpSize.Unspecified)
    ) {
        var value by remember { mutableStateOf("") }
        Column(Modifier.wrapContentSize().padding(24.dp)) {
            TextField(label = { Text("目录") }, value = value, onValueChange = { value = it })
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onConfirm(value) }) { Text("确定") }
        }
    }
}