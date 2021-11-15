package me.konyaco.neteasemusic.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.dp

val colors = lightColors(
    primary = Color(0xFFEC4141),
    primaryVariant = Color(0xFFB93232),
    secondary = Color(0xFFEC4141),
    secondaryVariant = Color(0xFFB93232)
)

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = colors) {
        CompositionLocalProvider(
            LocalIndication provides remember { MyIndication() },
            LocalScrollbarStyle provides remember {
                ScrollbarStyle(
                    minimalHeight = 120.dp,
                    thickness = 5.dp,
                    shape = RoundedCornerShape(3.dp),
                    hoverDurationMillis = 200,
                    hoverColor = Color.Black.copy(0.12f),
                    unhoverColor = Color.Black.copy(0.12f),
                )
            }
        ) {
            content()
        }
    }
}

class MyIndication : Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        val isHovered = interactionSource.collectIsHoveredAsState()
        val isFocused = interactionSource.collectIsFocusedAsState()
        val isPressed = interactionSource.collectIsPressedAsState()
        val isDragged = interactionSource.collectIsDraggedAsState()
        val instance = remember(interactionSource) {
            MyIndicationInstance(isHovered, isFocused, isPressed, isDragged)
        }
        return instance
    }

    private class MyIndicationInstance(
        private val isHovered: State<Boolean>,
        private val isFocused: State<Boolean>,
        private val isPressed: State<Boolean>,
        private val isDragged: State<Boolean>
    ) : IndicationInstance {
        override fun ContentDrawScope.drawIndication() {
            drawContent()
            if (isHovered.value) drawRect(color = Color.Black.copy(0.02f), Offset.Zero, size, 1f)
        }
    }
}

