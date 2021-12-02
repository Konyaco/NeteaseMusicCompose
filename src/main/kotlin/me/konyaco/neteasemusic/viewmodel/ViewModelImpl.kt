package me.konyaco.neteasemusic.viewmodel

import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import me.konyaco.neteasemusic.Config
import me.konyaco.neteasemusic.MusicPlayer
import java.io.File

class ViewModelImpl private constructor(
    private val musicPlayer: MusicPlayer,
    private val config: Config
) : ViewModel, PlayViewModel, LocalAndDownloadViewModel {

    companion object {
        private val instance = ViewModelImpl(MusicPlayer(), Config(File("config.prop")))

        fun getInstance(): ViewModel {
            return instance
        }
    }

    override val isLocalSongListRefreshing = MutableStateFlow(false)
    override val localSongList: MutableStateFlow<List<ViewModel.LocalSongInfo>> = MutableStateFlow(emptyList())
    override val localSongDirectory: MutableStateFlow<String> = MutableStateFlow(getMusicDir())
    override val playingState = MutableStateFlow<ViewModel.PlayingState?>(null)
    override val playList = MutableStateFlow<List<ViewModel.SongInfo>>(emptyList())
    override val playMode = MutableStateFlow(ViewModel.PlayMode.CYCLE)

    private var orderList: List<Int> = emptyList()
    private var orderIndex = 0
    private var cycle: Boolean = isCycle()

    private val modes = ViewModel.PlayMode.values()
    private var currentModeIndex = 0

    private val scope = CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    })

    override fun play() {
        scope.launch {
            mPlay()
        }
    }

    private fun getMusicDir(): String {
        return config.getMusicDir() ?: defaultMusicDir()
    }

    private fun defaultMusicDir(): String {
        return File(System.getProperty("user.home")).resolve("Music").absolutePath
    }

    private suspend fun mPlay() {
        val state = playingState.value
        if (state == null) {
            setMusicPlayWithCurrentSong()
        }
        musicPlayer.play()
        setPlayState(true)
    }

    private fun setPlayState(play: Boolean) {
        playingState.value?.isPlaying?.value = play
    }

    override fun pause() {
        scope.launch {
            mPause()
        }
    }

    private fun mPause() {
        musicPlayer.pause()
        setPlayState(false)
    }

    override fun next() {
        scope.launch {
            setCurrentSongToNext()
            mPlay()
        }
    }

    suspend fun setCurrentSongToNext() {
        increaseOrder()
        setMusicPlayWithCurrentSong()
    }

    override fun previous() {
        scope.launch {
            setCurrentSongToPrevious()
            play()
        }
    }

    private suspend fun setCurrentSongToPrevious() {
        decreaseOrder()
        setMusicPlayWithCurrentSong()
    }

    private fun increaseOrder() {
        orderIndex += 1
        if (orderIndex >= orderList.size) {
            if (cycle) {
                orderIndex = 0
            } else {
                orderIndex = orderList.size - 1
            }
        }
    }

    private fun decreaseOrder() {
        orderIndex -= 1
        if (orderIndex < 0) {
            if (cycle) {
                orderIndex = orderList.size - 1
            } else {
                orderIndex = 0
            }
        }
    }

    override fun selectSong(song: ViewModel.LocalSongInfo) {
        scope.launch {
            setCurrentSong(song)
            mPlay()
        }
    }

    private suspend fun setCurrentSong(song: ViewModel.LocalSongInfo) {
        var index = getOrderIndexInPlayList(song)
        if (index == -1) { // If not found, add playList and set the index again.
            // TODO: 2021/12/2
            replacePlayListWithLocalSongs()
            parsePlayOrder()
            index = getOrderIndexInPlayList(song)
        }
        if (index != -1) {
            orderIndex = index
            setMusicPlayWithCurrentSong()
            // If found, set orderIndex to selected son
        } else {
            // TODO: 2021/12/2 This is a workaround for single_cycle mode, consider to rethink the way for play_mode
            if (playMode.value == ViewModel.PlayMode.SINGLE_CYCLE) {
                val songIndex = playList.value.indexOf(song)
                orderList = listOf(songIndex)
            }
        }
    }

    private fun getOrderIndexInPlayList(song: ViewModel.LocalSongInfo): Int {
        val songIndex = playList.value.indexOf(song)
        return orderList.indexOf(songIndex)
    }

    private suspend fun setMusicPlayWithCurrentSong() = coroutineScope {
        val currentPlayingIndex = getCurrentPlayingIndex() ?: return@coroutineScope
        val currentSong = playList.value[currentPlayingIndex] as? ViewModel.LocalSongInfo
            ?: return@coroutineScope
        val song = musicPlayer.parse(currentSong.file)
        musicPlayer.setSong(song)

        val state = ViewModel.PlayingState(
            songInfo = currentSong,
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
                onEnd()
            }
        }
        playingState.value = state
    }

    private fun onEnd() = next()

    override fun rollPlayMode() {
        scope.launch {
            mRollPlayMode()
            parsePlayOrder()
        }
    }

    private fun mRollPlayMode() {
        if (currentModeIndex == modes.size - 1) currentModeIndex = 0
        else currentModeIndex += 1
        playMode.value = modes[currentModeIndex]
    }

    override fun changeProgress(progress: Float) {
        scope.launch {
            musicPlayer.setProgress(progress)
        }
    }

    override fun refreshLocalSongList() {
        scope.launch {
            isLocalSongListRefreshing.value = true
            try {
                mRefreshLocalSongList()
            } finally {
                isLocalSongListRefreshing.value = false
            }
        }
    }

    private suspend fun mRefreshLocalSongList() {
        val files = File(localSongDirectory.value).listFiles()

        if (files == null) {
            isLocalSongListRefreshing.value = false
            // TODO: 2021/11/15 Show error state
            return // Directory not found
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
            ViewModel.LocalSongInfo(
                name = song.title ?: it.nameWithoutExtension,
                author = song.artist ?: "未知歌手",
                album = song.album ?: "未知专辑",
                totalDurationMillis = song.durationMillis,
                size = it.length(),
                file = it
            )
        }
        localSongList.emit(songs)
    }

    override fun changeDirectory(directory: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                config.storeMusicDir(directory)
            }
            localSongDirectory.value = directory
        }
    }

    override fun addToNext(song: ViewModel.LocalSongInfo) {
        scope.launch {
            insertSongToPlaylist(song)
            parsePlayOrder()
        }
    }

    /**
     * @return null means there is no song playing now.
     */
    private fun getCurrentPlayingIndex(): Int? = orderList.getOrNull(orderIndex)

    private fun insertSongToPlaylist(song: ViewModel.LocalSongInfo) {
        val playIndex = getCurrentPlayingIndex()

        if (playIndex == null) {
            playList.value = listOf(song)
        } else {
            val tempList = playList.value.toMutableList()
            for (i in tempList.size + 1..playIndex + 1) {
                tempList[i] = tempList[i - 1]
            }
            tempList[playIndex + 1] = song
            playList.value = tempList
        }
    }

    override fun addToPlayList(song: ViewModel.LocalSongInfo) {
        TODO("Not yet implemented")
    }

    override fun removeFromPlayList(song: ViewModel.LocalSongInfo) {
        TODO("Not yet implemented")
    }

    override fun replacePlayList() {
        scope.launch {
            replacePlayListWithLocalSongs()
            parsePlayOrder()
            resetOrder()
            setMusicPlayWithCurrentSong()
        }
    }

    private fun replacePlayListWithLocalSongs() {
        playList.value = localSongList.value
    }

    private fun resetOrder() {
        orderIndex = 0
    }

    override fun replacePlayListAndPlay() {
        scope.launch {
            replacePlayListWithLocalSongs()
            parsePlayOrder()
            resetOrder()
            setMusicPlayWithCurrentSong()
            mPlay()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun parsePlayOrder() {
        val currentPlayingIndex = getCurrentPlayingIndex()
        when (playMode.value) {
            ViewModel.PlayMode.LIST, ViewModel.PlayMode.CYCLE, ViewModel.PlayMode.HEARTBEAT -> {
                orderList = List(playList.value.size) { it }
                orderIndex = currentPlayingIndex ?: 0
            }
            ViewModel.PlayMode.SINGLE_CYCLE -> {
                // Only contains current playing song
                if (currentPlayingIndex != null) {
                    orderList = listOf(currentPlayingIndex)
                    orderIndex = 0
                } else {
                    // If there is no current song, just return.
                    return
                }
            }
            ViewModel.PlayMode.RANDOM -> {
                val tempList = List(playList.value.size) { it }.shuffled()
                val index = tempList.firstOrNull { it == currentPlayingIndex } ?: 0
                orderIndex = index
                orderList = tempList
            }
        }
    }

    private fun isCycle(): Boolean {
        return when (playMode.value) {
            ViewModel.PlayMode.CYCLE, ViewModel.PlayMode.SINGLE_CYCLE -> true
            else -> false
        }
    }
}