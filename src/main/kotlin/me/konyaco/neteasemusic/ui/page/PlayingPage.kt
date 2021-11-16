package me.konyaco.neteasemusic.ui.page

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.konyaco.neteasemusic.viewmodel.ViewModel
import java.awt.Cursor
import kotlin.math.roundToInt

@Composable
fun PlayPage(
    viewModel: ViewModel,
    visible: Boolean,
    onClose: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val transition = updateTransition(if (visible) "visible" else "invisible")
        val offset by transition.animateDp(
            transitionSpec = {
                if (targetState == "visible") spring()
                else tween(170, easing = FastOutLinearInEasing)
            },
            targetValueByState = {
                if (it == "visible") 0.dp
                else maxHeight
            }
        )
        // If page is invisible and the animation was end, do not render the page.
        if (transition.targetState == "visible" || transition.currentState == "visible") {
            // Background container
            Box(Modifier.fillMaxSize()
                .offset(y = offset)
                .background(
                    // TODO: Pick color from album image
                    Brush.verticalGradient(
                        remember { listOf(Color(0xFFD8D0D1), Color.White) }
                    )
                )
                .pointerInput(Unit) {
                    // Just for consuming pointer input
                }
            ) {
                Content(onClose, viewModel)
            }
        }
    }
}

@Composable
private fun Content(onClose: () -> Unit, viewModel: ViewModel) {
    val songInfo by viewModel.playingState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.height(60.dp).padding(horizontal = 21.dp), verticalAlignment = Alignment.CenterVertically) {
            FoldDownButton(onClose)
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(top = 32.dp)) {
            songInfo?.let { playingState ->
                // Music title and artist
                Column(
                    modifier = Modifier.wrapContentSize().align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.widthIn(max = 400.dp),
                        text = playingState.songInfo.name,
                        fontSize = 28.sp,
                        color = Color.Black.copy(0.9f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        modifier = Modifier.widthIn(max = 280.dp),
                        text = "${playingState.songInfo.author} - ${playingState.songInfo.album}",
                        fontSize = 14.sp,
                        color = Color.Black.copy(0.5f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }

                // Animation album and lyric
                Row(Modifier.fillMaxHeight().fillMaxWidth(0.7f).align(Alignment.TopCenter)) {
                    Box(
                        modifier = Modifier.weight(1f).padding(top = 32.dp)
                    ) {
                        Disc(playingState.cover.collectAsState().value, playingState.isPlaying.collectAsState().value)
                    }
                    Spacer(Modifier.widthIn(64.dp))
                    // TODO: 2021/11/16 Scrolling lyric
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 104.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "暂未支持", color = Color.Black.copy(0.5f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FoldDownButton(onClick: () -> Unit) {
    Image(
        modifier = Modifier.size(24.dp).clickable(onClick = onClick, role = Role.Button, onClickLabel = "Close")
            .pointerHoverIcon(remember { PointerIcon(Cursor(Cursor.HAND_CURSOR)) }),
        painter = painterResource("icons/page/play/fold_down.svg"),
        colorFilter = ColorFilter.tint(Color.Black.copy(0.7f)),
        contentDescription = null
    )
}

@Composable
private fun Disc(cover: ImageBitmap?, playing: Boolean) {
    val transition = updateTransition(playing)
    val needleRotation by transition.animateFloat(
        transitionSpec = { tween() },
        targetValueByState = {
            if (it) 0f
            else -35f
        }
    )

    val animatable = remember { Animatable(0f) }
    val discRotation by animatable.asState()

    // Infinite rotation on playing.
    LaunchedEffect(playing) {
        if (playing) {
            launch {
                while (isActive) {
                    val duration = (40_000 * (1 - (animatable.value / 360f))).roundToInt()
                    animatable.animateTo(
                        targetValue = 360f,
                        tween(
                            easing = LinearEasing,
                            durationMillis = duration
                        )
                    )
                    animatable.snapTo(0f)
                }
            }
        } else {
            animatable.stop()
        }
    }

    Box {
        // Needle
        Box(Modifier
            .zIndex(2f)
            .padding(start = 118.dp)
            .height(124.dp)
            .graphicsLayer {
                rotationZ = needleRotation
                transformOrigin = TransformOrigin(0.15f, 0.1f)
            }
        ) {
            Image(
                modifier = Modifier.fillMaxHeight(),
                painter = painterResource("res/play/play_needle.png"),
                contentDescription = "Dict needle",
                contentScale = ContentScale.FillHeight
            )
        }

        Box(
            modifier = Modifier.zIndex(1f)
                .padding(top = 72.dp)
                .rotate(discRotation)
                .size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Album
            cover?.let {
                Image(
                    modifier = Modifier.fillMaxSize().padding(40.dp).clip(CircleShape),
                    bitmap = it,
                    contentDescription = "Album cover",
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Medium
                )
            }
            // Border
            Image(painterResource("res/play/play_disc.webp"), "Play disc")
            // Gray Border
            Box(
                modifier = Modifier.fillMaxSize()
                    .clip(CircleShape)
                    .border(width = 10.dp, color = Color(0xFFDFDFDF), shape = CircleShape)
            )
        }
    }
}