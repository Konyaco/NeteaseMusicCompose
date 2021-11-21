package me.konyaco.neteasemusic.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.konyaco.neteasemusic.MusicPlayer
import java.io.File
import java.util.*

class ViewModel {
    private val musicPlayer = MusicPlayer()

    val isLocalSongListRefreshing = MutableStateFlow(false)
    val localSongList: MutableStateFlow<List<LocalSongInfo>> = MutableStateFlow(emptyList())
    val localSongDirectory: MutableStateFlow<String> = MutableStateFlow(getMusicDir())

    val playingState = MutableStateFlow<PlayingState?>(null)
    val playlist = MutableStateFlow<List<SongInfo>>(emptyList())

    val playMode = MutableStateFlow(PlayMode.CYCLE)

    enum class PlayMode {
        CYCLE, SINGLE_CYCLE, HEARTBEAT, LIST, RANDOM
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    data class PlayingState(
        val songInfo: SongInfo,
        var indexInPlayList: MutableStateFlow<Int>,
        val cover: MutableStateFlow<ImageBitmap?>,
        val currentTimeStampMillis: MutableStateFlow<Long>,
        val isPlaying: MutableStateFlow<Boolean>
    )

    open class SongInfo(
        val name: String,
        val author: String,
        val album: String,
        val totalDurationMillis: Long
    )

    class LocalSongInfo(
        name: String,
        author: String,
        album: String,
        totalDurationMillis: Long,
        val size: Long,
        val file: File
    ) : SongInfo(name, author, album, totalDurationMillis)

    private fun getMusicDir(): String {
        var musicDir: String? = null
        val configFile = File("config.prop")
        if (configFile.exists() && configFile.isFile) {
            val prop = Properties()
            prop.load(configFile.reader())
            musicDir = prop.getProperty("local_music_dir")
        }
        if (musicDir == null) {
            musicDir = File(System.getProperty("user.home")).resolve("Music").absolutePath
        }
        return musicDir!!
    }

    fun play() {
        musicPlayer.play()
        playingState.value?.isPlaying?.value = true
    }

    fun pause() {
        musicPlayer.pause()
        playingState.value?.isPlaying?.value = false
    }

    fun next() {
        // TODO: 2021/11/15  
    }

    fun previous() {
        // TODO: 2021/11/15  
    }

    private val modes = PlayMode.values()
    private var mode = 0

    fun changePlayMode() {
        // TODO: 2021/11/15
        if (mode == modes.size - 1) mode = 0
        else mode += 1
        playMode.value = modes[mode]
    }

    fun changeProgress(progress: Float) {
        musicPlayer.setProgress(progress)
    }

    fun refreshSongList() {
        scope.launch {
            isLocalSongListRefreshing.value = true
            val files = File(localSongDirectory.value).listFiles()

            if (files == null) {
                isLocalSongListRefreshing.value = false
                // TODO: 2021/11/15 Show error state
                return@launch // Directory not found
            }

            val songs = files.filter {
                if (it.isFile) {
                    when (it.extension) {
                        "mp3", "ogg", "flac", "wav", "aac" -> true
                        else -> false
                    }
                } else false
            }.mapNotNull {
                val song = try {
                    musicPlayer.parse(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@mapNotNull null
                }
                song.release()
                LocalSongInfo(
                    name = song.title ?: it.nameWithoutExtension,
                    author = song.artist ?: "未知歌手",
                    album = song.album ?: "未知专辑",
                    totalDurationMillis = song.durationMillis,
                    size = it.length(),
                    file = it
                )
            }
            localSongList.emit(songs)
            isLocalSongListRefreshing.value = false
        }
    }

    fun changeDirectory(directory: String) {
        scope.launch {
            val configFile = File("config.prop")
            val prop = Properties().apply {
                setProperty("local_music_dir", directory)
            }
            prop.store(configFile.writer(), null)
            localSongDirectory.value = directory
            refreshSongList()
        }
    }

    fun addToPlaylist(localSongInfo: LocalSongInfo) {
        // TODO: 2021/11/15 Rewrite
        scope.launch {
            val song = musicPlayer.parse(localSongInfo.file)
            musicPlayer.setSong(song)
            val cover = MutableStateFlow<ImageBitmap?>(null)
            launch {
                cover.emit(song.coverImage?.toComposeImageBitmap())
            }
            val current = MutableStateFlow(0L)
            musicPlayer.setProgressListener { c, t ->
                current.value = c
            }
            playingState.value = PlayingState(
                songInfo = localSongInfo,
                indexInPlayList = MutableStateFlow(0),
                cover = cover,
                currentTimeStampMillis = current,
                isPlaying = MutableStateFlow(true)
            )
            musicPlayer.play()
        }
    }

    fun addAllToPlayList() {
        // TODO: 2021/11/15 
    }

    fun addAllToPlayListAndPlay() {
        // TODO: 2021/11/15
    }
}