package me.konyaco.neteasemusic

import java.io.File
import java.util.*

class Config(private val configFile: File) {
    companion object {
        private const val VERSION = "0"
    }

    fun getMusicDir(): String? {
        return if (!configFile.exists()) null
        else {
            val prop = Properties()
            configFile.reader().use {
                prop.load(it)
            }
            prop.getProperty("local_music_dir")
        }
    }

    fun storeMusicDir(directory: String) {
        if (!configFile.exists()) {
            val prop = Properties().apply {
                setProperty("local_music_dir", directory)
            }
            configFile.writer().use {
                prop.store(it, VERSION)
            }
        }
    }
}