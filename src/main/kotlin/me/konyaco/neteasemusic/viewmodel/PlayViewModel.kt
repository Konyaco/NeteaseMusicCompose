package me.konyaco.neteasemusic.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow

interface PlayViewModel {
    val playingState: MutableStateFlow<ViewModel.PlayingState?>
    val playMode: MutableStateFlow<ViewModel.PlayMode>

    fun play()
    fun pause()

    fun next()
    fun previous()

    fun rollPlayMode()
    fun changeProgress(progress: Float)
}