package me.konyaco.neteasemusic.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

sealed interface NavScreen {
    sealed class Main(val label: String) : NavScreen {
        companion object {
            val values: List<Main> = listOf(Discover, Radio, Video, Friends, Live, FM)
        }

        object Discover : Main("发现音乐")
        object Radio : Main("播客")
        object Video : Main("视频")
        object Friends : Main("朋友")
        object Live : Main("直播")
        object FM : Main("私人 FM")
    }

    @Stable
    sealed interface My : NavScreen {
        val label: String
        val icon: @Composable () -> Painter

        object Download : My {
            override val label: String = "本地与下载"
            override val icon: @Composable () -> Painter = @Composable { painterResource("icons/sidebar/download.svg") }
        }

        object History : My {
            override val label: String = "最近播放"
            override val icon: @Composable () -> Painter = @Composable { painterResource("icons/sidebar/history.svg") }
        }

        object Radio : My {
            override val label: String = "我的播客"
            override val icon: @Composable () -> Painter = @Composable { painterResource("icons/sidebar/radio.svg") }
        }

        object Cloud : My {
            override val label: String = "我的音乐云盘"
            override val icon: @Composable () -> Painter = @Composable { painterResource("icons/sidebar/cloud.svg") }
        }

        object FavIcon : My {
            override val label: String = "我的收藏"
            override val icon: @Composable () -> Painter = @Composable { painterResource("icons/sidebar/myfav.svg") }
        }

        companion object {
            val values: List<My> = listOf(Download, History, Radio, Cloud, FavIcon)
        }
    }

    object MyFav : NavScreen
    data class MySongList(val name: String, val public: Boolean, val id: Int) : NavScreen
    data class FavSongList(val name: String, val id: Int) : NavScreen
}

@Composable
fun SideNav(modifier: Modifier) {
    Box(modifier) {
        val scrollState = rememberScrollState()

        val mySongList = remember {
            listOf(
                NavScreen.MySongList("歌单 1", true, 0),
                NavScreen.MySongList("歌单 2", false, 1),
                NavScreen.MySongList("歌单 3", true, 2)
            )
        }
        val favSongList = remember {
            listOf(
                NavScreen.FavSongList("歌单 1", 0),
                NavScreen.FavSongList("歌单 2", 1),
                NavScreen.FavSongList("歌单 3", 2)
            )
        }

        var screen by remember { mutableStateOf<NavScreen>(NavScreen.My.Download) }

        Column(
            modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            NavScreen.Main.values.forEach { s ->
                NavItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = s.label,
                    selected = screen == s,
                    onSelectStateChange = { if (it) screen = s }
                )
            }
            TextDivider("我的音乐")
            NavScreen.My.values.forEach { item ->
                NavItem(
                    modifier = Modifier.fillMaxWidth(),
                    selected = screen == item,
                    onSelectStateChange = { if (it) screen = item },
                    leadIcon = {
                        Image(
                            modifier = Modifier.size(16.dp),
                            painter = item.icon(),
                            contentDescription = null
                        )
                    },
                    text = item.label,
                    tailIcon = {}
                )
            }
            var expandMySongList by remember { mutableStateOf(true) }
            TextDivider("创建的歌单", true, expandMySongList, { expandMySongList = it })
            if (expandMySongList) {
                FavItem(
                    modifier = Modifier.fillMaxWidth(),
                    selected = screen == NavScreen.MyFav,
                    onSelectStateChange = { screen = NavScreen.MyFav }
                )
                mySongList.forEach { item ->
                    SonglistItem(
                        name = item.name,
                        public = item.public,
                        selected = screen == item,
                        onSelectStateChange = { screen = item },
                    )
                }
            }
            var expandFavSongList by remember { mutableStateOf(true) }
            TextDivider("收藏的歌单", true, expandFavSongList, { expandFavSongList = it })
            if (expandFavSongList) favSongList.forEach { item ->
                SonglistItem(item.name, true, screen == item, { screen = item })
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(end = 1.dp),
            adapter = rememberScrollbarAdapter(scrollState),
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HeartbeatButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.size(42.dp, 24.dp),
        border = BorderStroke(width = Dp.Hairline, color = Color.Black.copy(0.12f)),
        shape = RoundedCornerShape(50),
        color = Color.White,
        onClick = onClick,
        onClickLabel = "Heartbeat mode"
    ) {
        Box {
            Image(
                modifier = Modifier.size(24.dp).align(Alignment.Center),
                painter = painterResource("icons/sidebar/heartbeat.svg"),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }
}

@Composable
private fun TextDivider(
    text: String,
    expandable: Boolean = false,
    expanded: Boolean = false,
    onCollapseStateChange: (Boolean) -> Unit = {},
    action: @Composable RowScope.() -> Unit = {}
) {
    Row(Modifier.height(32.dp).composed {
        if (expandable) {
            clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                onCollapseStateChange(!expanded)
            }
        } else Modifier
    }) {
        Text(
            modifier = Modifier.padding(start = 8.dp).align(Alignment.CenterVertically),
            text = text,
            fontSize = 13.sp,
            color = Color.Black.copy(0.5f)
        )
        if (expandable) {
            Spacer(Modifier.width(4.dp))
            Image(
                modifier = Modifier.size(7.dp, 5.dp).rotate(if (expanded) 0f else -90f)
                    .align(Alignment.CenterVertically),
                painter = painterResource("icons/sidebar/down.svg"),
                contentDescription = null
            )
        }
        action()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NavItem(
    modifier: Modifier,
    selected: Boolean,
    onSelectStateChange: (Boolean) -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier.clip(RoundedCornerShape(4.dp)).clickable { onSelectStateChange(!selected) }.pointerHoverIcon(remember {
            PointerIcon(Cursor(Cursor.HAND_CURSOR))
        }).height(37.dp)
            .background(if (selected) Color.Black.copy(0.04f) else Color.Transparent).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun NavItem(
    modifier: Modifier,
    selected: Boolean,
    onSelectStateChange: (Boolean) -> Unit,
    leadIcon: @Composable () -> Unit,
    text: String,
    tailIcon: @Composable () -> Unit
) {
    NavItem(
        modifier = modifier,
        selected = selected,
        onSelectStateChange = onSelectStateChange
    ) {
        leadIcon()
        Spacer(Modifier.width(4.dp))
        Text(modifier = Modifier.weight(1f), text = text, fontSize = 14.sp)
        tailIcon()
    }
}

@Composable
private fun FavItem(
    modifier: Modifier, selected: Boolean,
    onSelectStateChange: (Boolean) -> Unit
) {
    NavItem(
        modifier = modifier,
        selected = selected,
        onSelectStateChange = onSelectStateChange,
        leadIcon = {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource("icons/sidebar/like.svg"),
                contentDescription = null
            )
        },
        text = "我喜欢的音乐",
        tailIcon = {
            HeartbeatButton { }
        }
    )
}

@Composable
private fun SonglistItem(
    name: String,
    public: Boolean = true,
    selected: Boolean,
    onSelectStateChange: (Boolean) -> Unit
) {
    NavItem(
        modifier = Modifier.fillMaxWidth(),
        selected = selected,
        onSelectStateChange = onSelectStateChange,
        leadIcon = {
            Image(
                modifier = Modifier.size(20.dp),
                painter = if (public) painterResource("icons/sidebar/song.svg")
                else painterResource("icons/sidebar/lock.svg"),
                contentDescription = null
            )
        },
        text = name,
        tailIcon = {}
    )
}

@Composable
private fun NavItem(
    text: String,
    selected: Boolean,
    onSelectStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavItem(modifier, selected, onSelectStateChange) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically), text = text,
            fontSize = if (selected) 17.sp else 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}