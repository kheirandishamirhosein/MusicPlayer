package com.example.amirhoseinmusicplayer



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

/*
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

 */
import android.os.Build
import com.example.amirhoseinmusicplayer.data.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.data.mediaplayer.PlayMode
import com.example.amirhoseinmusicplayer.model.AudioModel
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PlaySongTest {
    private val mediaPlayer = AudioMediaPlayer
    @Test
    fun playSong() {

        val songsTest: List<AudioModel> = listOf(
            AudioModel(
                path = "/storage/emulated/0/Music/Telegram/Dorcci - Dorcci x Kagan - Before You Gone.mp3",
                title = "- Before You Gone",
                duration = 176875,
                id = "1000012581",
                artist = "Dorcci x Kagan",
                image = "3247560053718339483",
                formattedDuration = "02:56"
            ),
            AudioModel(
                path = "/storage/emulated/0/Music/Sia ft. Rihanna - Beautiful People.mp3",
                title = "Beautiful People",
                duration = 329143,
                id = "2978",
                artist = "Sia- Rihanna",
                image = "6539316500227728566",
                formattedDuration = "05:29"
            )
        )
        mediaPlayer.startPlaying(songsTest)
        assertEquals(PlayMode.PLAY, PlayMode.PLAY)
    }
}