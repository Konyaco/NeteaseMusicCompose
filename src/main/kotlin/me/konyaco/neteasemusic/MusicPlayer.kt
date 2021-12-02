package me.konyaco.neteasemusic

import me.konyaco.neteasemusic.MusicPlayer.ProgressListener
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import java.nio.charset.Charset
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread
import kotlin.properties.Delegates

class MusicPlayer {
    inner class Song internal constructor(private val file: File) {
        private val grabber: FFmpegFrameGrabber

        private var toPause = false

        internal var playThreadStarted = false

        internal var timeStamp = 0L

        var coverImage: BufferedImage? = null
            private set
        var title: String? = null
            private set
        var artist: String? = null
            private set
        var album: String? = null
            private set
        var durationMillis by Delegates.notNull<Long>()
            private set

        init {
            grabber = FFmpegFrameGrabber(file)
            grabber.charset = Charsets.UTF_8
            grabber.start()
            parseMetadata()
            parseCoverImage()
        }

        private fun parseCoverImage() {
            val frame = grabber.grabImage()
            if (frame != null) coverImage = Java2DFrameConverter().convert(frame)
            jump(0)
        }

        private fun parseMetadata() {
            val metadata = grabber.metadata
            artist = metadata["ARTIST"] ?: metadata["artist"]
            title = metadata["TITLE"] ?: metadata["title"]
            album = metadata["ALBUM"] ?: metadata["album"]
            durationMillis = grabber.lengthInTime / 1000L
        }

        /**
         * FFMPEG parses string with system default charset(e.g: GBK/GB2312 in windows-zh-cn), but the data may actually save with UTF-8 charset.
         * Encode the parsed string with system default charset, will get the original data.
         * Then, use UTF-8 charset to parse it.
         */
        private fun String.correctCharset(): String {
            val systemCharset = Charset.defaultCharset()
            val originData = toByteArray(systemCharset)
            return String(originData, Charsets.UTF_8)
        }

        private var thread: Thread? = null

        private val lock: Object = Object()

        fun play() {
            thread = thread {
                playThreadStarted = true
                toPause = false
                val audioFormat = AudioFormat(grabber.sampleRate.toFloat(), 16, grabber.audioChannels, true, true)
                val datainfo = DataLine.Info(SourceDataLine::class.java, audioFormat)
                val soundLine = AudioSystem.getLine(datainfo) as SourceDataLine
                soundLine.open(audioFormat)
                soundLine.start()
                while (!Thread.interrupted()) {
                    synchronized(lock) {
                        if (toPause) {
                            toPause = false
                            lock.wait()
                        }
                    }
                    val frame = grabber.grab()
                    if (frame == null) {
                        listener.onProgress(durationMillis, durationMillis)
                        return@thread // Play end
                    }
                    if (frame.types.contains(Frame.Type.AUDIO)) {
                        timeStamp = frame.timestamp
                        listener.onProgress(frame.timestamp / 1000L, durationMillis)
                        val sample = frame.samples.firstOrNull() as? ShortBuffer
                            ?: return@thread // Play end
                        val byteBuffer = ByteBuffer.allocate(sample.capacity() * 2)
                        for (i in 0 until sample.capacity()) {
                            byteBuffer.putShort(sample.get(i))
                        }
                        soundLine.write(byteBuffer.array(), 0, byteBuffer.capacity())
                    }
                }
                soundLine.close()
            }
        }

        @Synchronized
        fun pause() {
            toPause = true
        }

        fun resume() {
            synchronized(lock) {
                lock.notifyAll()
            }
        }

        @Synchronized
        fun jump(timeStampMillis: Long) {
            grabber.setTimestamp(timeStampMillis * 1000L, true)
        }

        @Synchronized
        fun release() {
            thread?.interrupt()
            grabber.release()
        }
    }

    private var currentSong: Song? = null
    private var listener: ProgressListener = ProgressListener { currentTimeMillis, totalTimeMillis -> }

    fun interface ProgressListener {
        fun onProgress(currentTimeMillis: Long, totalTimeMillis: Long)
    }

    fun setProgressListener(listener: ProgressListener) {
        this.listener = listener
    }

    fun parse(file: File): Song {
        return Song(file)
    }

    fun setSong(song: Song) {
        currentSong?.release()
        listener.onProgress(0, song.durationMillis)
        currentSong = song
    }

    fun play() {
        currentSong?.let {
            if (it.playThreadStarted) {
                it.resume()
            } else {
                it.play()
            }
        }
    }

    fun pause() {
        currentSong?.pause()
    }

    fun setProgress(float: Float) {
        currentSong?.let {
            setProgress((it.durationMillis * float).toLong())
        }
    }

    fun setProgress(timeStamp: Long) {
        currentSong?.jump(timeStamp)
    }
}