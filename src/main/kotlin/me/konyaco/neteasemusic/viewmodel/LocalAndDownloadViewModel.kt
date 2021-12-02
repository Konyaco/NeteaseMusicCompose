package me.konyaco.neteasemusic.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow

interface LocalAndDownloadViewModel {
    val isLocalSongListRefreshing: MutableStateFlow<Boolean>
    val localSongList: MutableStateFlow<List<ViewModel.LocalSongInfo>>
    val localSongDirectory: MutableStateFlow<String>

    fun replacePlayList()
    fun replacePlayListAndPlay()

    fun changeDirectory(directory: String)

    fun selectSong(song: ViewModel.LocalSongInfo)

    fun addToNext(song: ViewModel.LocalSongInfo)
    fun addToPlayList(song: ViewModel.LocalSongInfo)
    fun refreshLocalSongList()
}