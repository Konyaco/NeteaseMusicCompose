package viewmodel

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import me.konyaco.neteasemusic.viewmodel.ViewModel
import kotlin.test.BeforeTest
import kotlin.test.Test

class ViewModelTest {
    lateinit var viewModel: ViewModel

    @BeforeTest
    fun setup() {
        viewModel = ViewModel()
    }

    @Test
    fun test() = runBlocking {
        viewModel.refreshSongList()
        viewModel.localSongList.take(1).collect {
            println(it)
        }
    }
}