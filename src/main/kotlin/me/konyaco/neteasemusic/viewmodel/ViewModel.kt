package me.konyaco.neteasemusic.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import me.konyaco.neteasemusic.MusicPlayer
import java.io.File
import java.util.*

class ViewModel(private val musicPlayer: MusicPlayer) {
    val isLocalSongListRefreshing = MutableStateFlow(false)
    val localSongList: MutableStateFlow<List<LocalSongInfo>> = MutableStateFlow(emptyList())
    val localSongDirectory: MutableStateFlow<String> = MutableStateFlow(getMusicDir())

    val playingState = MutableStateFlow<PlayingState?>(null)
    val playList = MutableStateFlow<List<SongInfo>>(emptyList())

    val playMode = MutableStateFlow(PlayMode.CYCLE)

    enum class PlayMode {
        CYCLE, SINGLE_CYCLE, HEARTBEAT, LIST, RANDOM
    }

    private val scope = CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    })

    data class PlayingState(
        val songInfo: SongInfo,
        var indexInPlayList: MutableStateFlow<Int>,
        val cover: MutableStateFlow<ImageBitmap?>,
        val currentTimeStampMillis: MutableStateFlow<Long>,
        val isPlaying: MutableStateFlow<Boolean>
    )

    open class SongInfo(
        open val name: String,
        open val author: String,
        open val album: String,
        open val totalDurationMillis: Long
    )

    data class LocalSongInfo(
        override val name: String,
        override val author: String,
        override val album: String,
        override val totalDurationMillis: Long,
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

    fun play(): Deferred<Unit> = scope.async {
        val state = playingState.value
        if (state == null) {
            setSong()
        }
        musicPlayer.play()
        state?.isPlaying?.value = true
    }

    fun pause(): Deferred<Unit> = scope.async {
        musicPlayer.pause()
        playingState.value?.isPlaying?.value = false
    }

    private var orderList: List<Int> = emptyList()
    private var orderIndex = 0
    private var cycle: Boolean = isCycle()

    fun next(): Deferred<Unit> = scope.async {
        orderIndex += 1
        if (orderIndex >= orderList.size) {
            if (cycle) {
                orderIndex = 0
            } else {
                orderIndex = orderList.size - 1
            }
        }
        setSong()
        play().await()
    }

    fun previous(): Deferred<Unit> = scope.async {
        orderIndex -= 1
        if (orderIndex < 0) {
            if (cycle) {
                orderIndex = orderList.size - 1
            } else {
                orderIndex = 0
            }
        }
        setSong()
        play().await()
    }

    fun selectSong(song: LocalSongInfo): Deferred<Unit> = scope.async {
        // TODO: 2021/12/2
        val index = playList.value.indexOf(song)
        if (index == -1) { // If not found, add playList and set the index again.
            replacePlayList().await()
            selectSong(song).await()
        }
        // If found, set orderIndex to selected song
        orderIndex = orderList.indexOf(index)
        setSong()
    }

    private suspend fun setSong() = coroutineScope {
        val localSongInfo = localSongList.value[orderList[orderIndex]]
        val song = musicPlayer.parse(localSongInfo.file)
        musicPlayer.setSong(song)
        val state = PlayingState(
            songInfo = localSongInfo,
            indexInPlayList = MutableStateFlow(0),
            cover = MutableStateFlow(null),
            currentTimeStampMillis = MutableStateFlow(0L),
            isPlaying = MutableStateFlow(false)
        )
        launch {
            state.cover.emit(song.coverImage?.toComposeImageBitmap())
        }
        musicPlayer.setProgressListener { c, t ->
            state.currentTimeStampMillis.value = c
            if (c == t) {
                state.isPlaying.value = false
                next()
            }
        }
        playingState.value = state
    }

    private val modes = PlayMode.values()
    private var mode = 0

    fun changePlayMode() {
        if (mode == modes.size - 1) mode = 0
        else mode += 1
        playMode.value = modes[mode]
        parsePlayOrder()
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

    fun addToNext(localSongInfo: LocalSongInfo): Deferred<Unit> = scope.async {
        // TODO: 2021/12/2 May not work correctly
        val playIndex = orderList.getOrNull(orderIndex)
        if (playIndex == null) {
            playList.value = listOf(localSongInfo)
        } else {
            val tempList = playList.value.toMutableList()
            for (i in tempList.size + 1..playIndex + 1) {
                tempList[i] = tempList[i - 1]
            }
            tempList[playIndex + 1] = localSongInfo
            playList.value = tempList
        }
        parsePlayOrder()
    }

    fun removeFromPlayList(localSongInfo: LocalSongInfo) {
        // TODO: 2021/12/1
    }

    fun replacePlayList(): Deferred<Unit> = scope.async {
        playList.value = localSongList.value
        parsePlayOrder()
        orderIndex = 0
        setSong()
    }

    fun replacePlayListAndPlay() = scope.async {
        replacePlayList().await()
        play().await()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun parsePlayOrder() {
        val currentPlayingIndex = orderList.getOrElse(orderIndex) { 0 }
        when (playMode.value) {
            PlayMode.LIST, PlayMode.CYCLE, PlayMode.HEARTBEAT -> {
                orderList = List(playList.value.size) { it }
                orderIndex = currentPlayingIndex
            }
            PlayMode.SINGLE_CYCLE -> {
                orderList = listOf(currentPlayingIndex) // Only contains current playing song
                orderIndex = 0
            }
            PlayMode.RANDOM -> {
                orderList = List(playList.value.size) { it }.shuffled()
                orderIndex = orderList.first { it == currentPlayingIndex }
            }
        }
    }

    private fun isCycle(): Boolean {
        return when (playMode.value) {
            PlayMode.CYCLE, PlayMode.SINGLE_CYCLE -> true
            else -> false
        }
    }
}