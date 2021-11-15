package ui

import me.konyaco.neteasemusic.ui.sizeToText
import me.konyaco.neteasemusic.ui.timeStampToText
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilsTest {
    @Test
    fun testTimestampCovert() {
        assertTrue(timeStampToText(100400) == "01:40")
    }

    @Test
    fun testSizeConvert() {
        assertTrue(sizeToText(60 * 1024 * 1024) == "60.00MB")
    }
}