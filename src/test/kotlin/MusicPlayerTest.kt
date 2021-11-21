import me.konyaco.neteasemusic.MusicPlayer
import me.konyaco.neteasemusic.ui.timeStampToText
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import java.io.File
import kotlin.test.Test

class MusicPlayerTest {

    @Test
    fun test() {
        val song = MusicPlayer().parse(File("D:\\Users\\atwzj\\Music\\鹿乃 - DAYBREAK FRONTLINE.flac"))
        println(song.title)
        println(song.artist)
        println(song.album)
        println(timeStampToText(song.durationMillis / 1000L))
    }

    @Test
    fun test2() {
        val grabber = FFmpegFrameGrabber(File("D:\\Users\\atwzj\\Music\\鹿乃 - DAYBREAK FRONTLINE.flac"))
        grabber.start()

        val recorder = FFmpegFrameRecorder(
            File("D:\\Users\\atwzj\\Music\\鹿乃 - DAYBREAK FRONTLINE - 副本.flac"),
            grabber.audioChannels
        )
        recorder.start()

        recorder.audioCodec = grabber.audioCodec
        recorder.audioChannels = grabber.audioChannels
        recorder.audioBitrate = grabber.audioBitrate
        recorder.audioOptions = grabber.audioOptions
//        recorder.metadata = grabber.metadata

        recorder.setMetadata("ALBUM", "TestTest")

        var frame: Frame? = null
        while(true) {
            frame = grabber.grabSamples()
            if (frame != null) {
                if (frame.types.contains(Frame.Type.AUDIO)) {
                    recorder.recordSamples(*frame.samples)
                }
            } else break
        }


        recorder.release()
        grabber.release()
    }
}