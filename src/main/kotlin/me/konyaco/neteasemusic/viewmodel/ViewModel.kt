package me.konyaco.neteasemusic.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

interface ViewModel {
    val isLocalSongListRefreshing: MutableStateFlow<Boolean>
    val localSongList: MutableStateFlow<List<LocalSongInfo>>
    val localSongDirectory: MutableStateFlow<String>
    val playList: MutableStateFlow<List<SongInfo>>

    data class PlayingState(
        val songInfo: SongInfo,
        var indexInPlayList: MutableStateFlow<Int>,
        val cover: MutableStateFlow<ImageBitmap?>,
        val currentTimeStampMillis: MutableStateFlow<Long>,
        val isPlaying: MutableStateFlow<Boolean>
    )

    open class SongInfo(
        open val name: String, open val author: String, open val album: String, open val totalDurationMillis: Long
    )

    data class LocalSongInfo(
        override val name: String,
        override val author: String,
        override val album: String,
        override val totalDurationMillis: Long,
        val size: Long,
        val file: File
    ) : SongInfo(name, author, album, totalDurationMillis)

    enum class PlayMode {
        CYCLE, SINGLE_CYCLE, HEARTBEAT, LIST, RANDOM
    }

    fun removeFromPlayList(song: LocalSongInfo)
}