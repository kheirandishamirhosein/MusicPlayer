package com.example.amirhoseinmusicplayer.data.mediaplayer

import android.app.Activity
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.amirhoseinmusicplayer.model.AudioModel
import java.util.*

object AudioMediaPlayer : MediaPlayer() {
    private lateinit var originalSongs: List<AudioModel>
    private lateinit var songs: List<AudioModel>
    private var currentIndex = 0
    private var onRepeat = false
    private var onShuffle = false
    private var mode: PlayMode = PlayMode.STOP
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            calculateStatus()
            if (mode != PlayMode.STOP) {
                handler.postDelayed(this, 1000)
            }
        }
    }
    private val statusLiveData = MutableLiveData<PlayerStatus>()
    val status: LiveData<PlayerStatus> = statusLiveData

    fun startPlaying(songs: List<AudioModel>, index: Int = 0) {
        currentIndex = index
        originalSongs = songs
        AudioMediaPlayer.songs = songs
        //
        play(songs[currentIndex])
        registerOnCompletionListener()
        startRunnable()
    }

    private fun startRunnable() {
        handler.post(runnable)
    }

    private fun registerOnCompletionListener() {
        setOnCompletionListener {
            if (onRepeat) {
                play(songs[currentIndex])
                seekTo(0)
                if (onShuffle) {
                    shuffleStatus()
                }
            } else {
                playNext()
            }
        }
    }

    private fun play(audioModel: AudioModel) {
        mode = PlayMode.PLAY
        reset()
        setDataSource(audioModel.path)
        prepare()
        start()
        calculateStatus()
    }

    fun toggle() {
        if (isPlaying) {
            mode = PlayMode.PAUSE
            pause()
        } else {
            mode = PlayMode.PLAY
            start()
        }
        calculateStatus()
    }

    fun playNext() {
        if (onShuffle) {
            shuffleStatus()
        } else {
            if (currentIndex == songs.lastIndex) {
                return
            }
            currentIndex += 1
            play(songs[currentIndex])
            calculateStatus()
        }
    }

    fun playPrevious() {
        if (currentIndex == 0) {
            return
        }
        currentIndex -= 1
        play(songs[currentIndex])
        calculateStatus()
    }

    fun repeat(activity: Activity) {
        onRepeat = !onRepeat
        if (onRepeat) {
            Toast.makeText(activity, "Repeat ON", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Repeat OFF", Toast.LENGTH_SHORT).show()
        }
        calculateStatus()
    }

    fun shuffle(activity: Activity) {
        onShuffle = !onShuffle
        if (onShuffle) {
            Toast.makeText(activity, "Shuffle ON", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Shuffle OFF", Toast.LENGTH_SHORT).show()
        }
        calculateStatus()
    }

    private fun shuffleStatus() {
        if (onShuffle) {
            val random = Random()
            currentIndex = random.nextInt(songs.size - 1 - 0 + 1) + 0
            play(songs[currentIndex])
        } else {
            if (currentIndex < (songs.size - 1)) {
                play(songs[currentIndex + 1])
                currentIndex += 1
            } else {
                play(songs[0])
                currentIndex = 0
            }
        }
    }

    override fun stop() {
        handler.removeCallbacks(runnable)
        mode = PlayMode.STOP
        stop()
        release()
        calculateStatus()
    }

    private fun calculateStatus() {
        val status = PlayerStatus(
            mode = mode,
            currentPosition = currentPosition,
            onRepeat = onRepeat,
            onShuffle = onShuffle,
            currentSong = songs[currentIndex]
        )
        statusLiveData.value = status
    }
}