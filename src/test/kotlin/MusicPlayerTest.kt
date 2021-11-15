import me.konyaco.neteasemusic.MusicPlayer
import me.konyaco.neteasemusic.ui.timeStampToText
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
}