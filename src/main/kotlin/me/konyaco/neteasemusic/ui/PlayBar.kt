package me.konyaco.neteasemusic.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.konyaco.neteasemusic.viewmodel.ViewModel
import java.awt.Cursor
import kotlin.math.roundToInt

@Composable
fun PlayBar(
    viewModel: ViewModel,
    onAlbumImageClick: () -> Unit
) {
    val playingState by viewModel.playingState.collectAsState()
    val playMode by viewModel.playMode.collectAsState()

    Box(Modifier.background(Color.White).fillMaxWidth().height(72.dp)) {
        Divider(Modifier.fillMaxWidth().height(1.dp))

        Box(Modifier.align(Alignment.CenterStart)) {
            var liked by remember { mutableStateOf(false) } // TODO: 2021/11/15 Move this state to ViewModel
            playingState?.let {
                SongInfo(
                    it.songInfo.name,
                    it.songInfo.author,
                    liked = liked,
                    { liked = it },
                    playingState?.cover?.collectAsState()?.value,
                    onAlbumImageClick = onAlbumImageClick
                )
            }
        }

        Box(Modifier.align(Alignment.Center)) {
            // TODO: Move to ViewModel
            var lyricOn by remember { mutableStateOf(false) }

            ControlPanel(
                modifier = Modifier.align(Alignment.Center),
                onNextClick = { viewModel.next() },
                onLastClick = { viewModel.previous() },
                mode = playMode,
                onModeClick = { viewModel.changePlayMode() },
                lyricOn = lyricOn,
                onLyricStateChange = { lyricOn = it },
                isPlaying = playingState?.isPlaying?.collectAsState()?.value ?: false,
                onPlayStateChange = { if (it) viewModel.play() else viewModel.pause() },
                currentTimeMillis = playingState?.currentTimeStampMillis?.collectAsState()?.value ?: 0,
                totalTimeMillis = playingState?.songInfo?.totalDurationMillis ?: 1,
                onProgressChange = { viewModel.changeProgress(it) }
            )
        }

        Box(Modifier.align(Alignment.CenterEnd)) {
            Actions()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SongInfo(
    name: String,
    author: String,
    liked: Boolean,
    onLikeStateChange: (Boolean) -> Unit,
    cover: Painter?,
    onAlbumImageClick: () -> Unit
) {
    Row(
        Modifier.fillMaxHeight().width(280.dp).padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumButton(cover, onAlbumImageClick)
        Spacer(Modifier.width(12.dp))
        // Song title and artist
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.sizeIn(maxWidth = 180.dp),
                    text = name,
                    fontSize = 15.sp,
                    color = Color.Black.copy(0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Like Button
                Image(
                    modifier = Modifier.size(24.dp).clickable { onLikeStateChange(!liked) },
                    painter =
                    if (liked) painterResource("icons/playbar/liked.svg")
                    else painterResource("icons/playbar/like.svg"),
                    contentDescription = "like"
                )
            }
            Text(
                modifier = Modifier.sizeIn(maxWidth = 180.dp),
                text = author,
                fontSize = 13.sp,
                color = Color.Black.copy(0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AlbumButton(albumImage: Painter?, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    // Album Cover Image
    Box(
        Modifier.clip(RoundedCornerShape(4.dp))
            .size(48.dp)
            .background(Color.Black.copy(0.08f))
            .clickable(
                interactionSource = interactionSource,
                onClick = onClick,
                indication = LocalIndication.current
            )
            .pointerHoverIcon(remember { PointerIcon(Cursor(Cursor.HAND_CURSOR)) })
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = albumImage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            albumImage?.let {
                Image(
                    painter = it,
                    modifier = Modifier.fillMaxSize().composed {
                        if (isHovered) {
                            blur(2.dp)
                        } else this
                    },
                    contentDescription = "Cover image of song",
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (isHovered) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.24f))) // Half-transparent mask on hover

            // Fold up icon
            Image(
                modifier = Modifier.size(28.dp).align(Alignment.Center),
                painter = painterResource("icons/playbar/fold_up.svg"),
                contentDescription = "Fold up",
                colorFilter = ColorFilter.tint(Color.White.copy(0.9f))
            )
        }
    }
}

@Composable
private fun IconButton(icon: Painter, onClickLabel: String?, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        Modifier.size(32.dp).clickable(
            indication = null,
            interactionSource = interactionSource,
            onClick = onClick,
            onClickLabel = onClickLabel,
            role = Role.Button
        )
    ) {
        Image(
            modifier = Modifier.size(24.dp).align(Alignment.Center),
            painter = icon,
            colorFilter = ColorFilter.tint(if (isHovered) Color(0xFFEF6767) else Color.Black),
            contentDescription = onClickLabel
        )
    }
}

@Composable
private fun modeToIcon(mode: ViewModel.PlayMode): Painter {
    return painterResource(
        when (mode) {
            ViewModel.PlayMode.CYCLE -> "icons/playbar/mode_cycle.svg"
            ViewModel.PlayMode.SINGLE_CYCLE -> "icons/playbar/mode_cycle1.svg"
            ViewModel.PlayMode.LIST -> "icons/playbar/mode_list.svg"
            ViewModel.PlayMode.RANDOM -> "icons/playbar/mode_random.svg"
            ViewModel.PlayMode.HEARTBEAT -> "icons/playbar/mode_heartbeat.svg"
        }
    )
}

@Composable
private fun ControlPanel(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onPlayStateChange: (playing: Boolean) -> Unit,
    onNextClick: () -> Unit,
    onLastClick: () -> Unit,
    mode: ViewModel.PlayMode,
    onModeClick: () -> Unit,
    lyricOn: Boolean,
    onLyricStateChange: (Boolean) -> Unit,
    currentTimeMillis: Long,
    totalTimeMillis: Long,
    onProgressChange: (Float) -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(modeToIcon(mode), "Mode", onModeClick)
            IconButton(painterResource("icons/playbar/last.svg"), "Last", onLastClick)
            PlayButton(isPlaying) { onPlayStateChange(!isPlaying) }
            IconButton(painterResource("icons/playbar/next.svg"), "Next", onNextClick)
            IconButton(
                if (lyricOn) painterResource("icons/playbar/lyric_on.svg")
                else painterResource("icons/playbar/lyric_off.svg"), "Turn on Lyric"
            ) { onLyricStateChange(!lyricOn) }
        }
        Spacer(Modifier.height(2.dp))


        val progress by remember(
            currentTimeMillis,
            totalTimeMillis
        ) { mutableStateOf(currentTimeMillis / totalTimeMillis.toFloat()) }

        ProgressBar(
            progress = progress, onProgressChange = {
                onProgressChange(it)
            }, totalDurationMillis = totalTimeMillis
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayButton(playing: Boolean, onStateChange: (Boolean) -> Unit) {
    Box(Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(0.06f))
        .clickable { onStateChange(!playing) }
        .pointerHoverIcon(remember { PointerIcon(Cursor(Cursor.HAND_CURSOR)) }, true)
    ) {
        Image(
            modifier = Modifier.size(24.dp).align(Alignment.Center),
            painter = if (playing) painterResource("icons/playbar/pause.svg")
            else painterResource("icons/playbar/play.svg"),
            contentDescription = if (playing) "Pause" else "Play"
        )
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    totalDurationMillis: Long,
    onProgressChange: (Float) -> Unit
) {
    var uiProgress by remember { mutableStateOf(progress) }
    var changing by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        if (!changing) {
            uiProgress = progress
        }
    }

    ProgressBar(progress = uiProgress,
        onProgressChange = {
            changing = true
            uiProgress = it
        },
        onProgressConfirm = {
            onProgressChange(uiProgress)
            changing = false
        },
        progressText = remember(uiProgress) { timeStampToText((uiProgress * totalDurationMillis).toLong()) },
        totalDurationText = remember(totalDurationMillis) { timeStampToText(totalDurationMillis) }
    )
}

@Composable
private fun ProgressBar(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onProgressConfirm: () -> Unit,
    progressText: String,
    totalDurationText: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = progressText, fontSize = 12.sp, color = Color.Black.copy(0.5f))
        Spacer(Modifier.width(8.dp))
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        var isDragging by remember { mutableStateOf(false) }

        BoxWithConstraints(Modifier.height(18.dp).width(350.dp)) {
            val constraints = constraints
            Box(
                modifier = Modifier.fillMaxSize().hoverable(interactionSource)
                    .composed {
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        draggable(
                            state = rememberDraggableState {
                                offset = Offset(x = offset.x + it, y = offset.y)
                                val progress = (offset.x / constraints.maxWidth).coerceIn(0f, 1f)
                                onProgressChange(progress)
                            },
                            interactionSource = interactionSource,
                            onDragStarted = {
                                isDragging = true
                                offset = it
                            }, onDragStopped = {
                                isDragging = false
                                onProgressConfirm()
                            }, orientation = Orientation.Horizontal
                        )
                    }
                    .pointerInput(Unit) {
                        forEachGesture {
                            detectTapGestures {
                                val progress = (it.x / constraints.maxWidth).coerceIn(0f, 1f)
                                onProgressChange(progress)
                                onProgressConfirm()
                            }
                        }
                    }
            ) {
                val focus = isHovered || isDragging
                val height = if (focus) (5.5).dp else 3.dp
                // Background
                Box(
                    Modifier.align(Alignment.CenterStart).clip(RoundedCornerShape(50))
                        .height(height).fillMaxWidth().background(Color(0xFFCDCDCD))
                )
                // Progress
                Box(
                    Modifier.align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(50))
                        .height(height).fillMaxWidth(progress)
                        .background(Color(0xFFFF4E4E))
                )
                // Indicator
                if (focus) Box(
                    Modifier.align(Alignment.CenterStart)
                        .size(9.dp)
                        .offset {
                            val offset = (constraints.maxWidth * progress - (5 * density)).roundToInt()
                            IntOffset(x = offset, y = 0)
                        }
                ) {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFEC4141)))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(text = totalDurationText, fontSize = 12.sp, color = Color.Black.copy(0.5f))
    }
}

@Composable
private fun Actions() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(end = 21.dp)) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource("icons/playbar/soundeffect.svg"),
            contentDescription = "Sound Effect"
        )
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource("icons/playbar/sound1.svg"),
            contentDescription = "Sound Volume"
        )
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource("icons/playbar/listen_together.svg"),
            contentDescription = "Listen Together"
        )
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource("icons/playbar/playlist.svg"),
            contentDescription = "Play List"
        )
    }
}