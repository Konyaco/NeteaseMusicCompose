package me.konyaco.neteasemusic.ui

import java.time.Duration

fun timeStampToText(millis: Long): String {
    val seconds = Duration.ofMillis(millis).seconds
    val mm = "%02d".format(seconds / 60)
    val ss = "%02d".format(seconds % 60)
    return "$mm:$ss"
}

fun sizeToText(size: Long): String {
    return if (size < 1024L) {
        "${size}B"
    } else if (size < 1024L * 1024L) {
        val tmp = size / 1024f
        "%.2fKB".format(tmp)
    } else if (size < 1024L * 1024L * 1024L) {
        val tmp = size / 1024L / 1024f
        "%.2fMB".format(tmp)
    } else {
        val tmp = size / 1024L / 1024L / 1024f
        "%.2fGB".format(tmp)
    }
}