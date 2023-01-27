package com.example.amirhoseinmusicplayer.mediaplayer

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.amirhoseinmusicplayer.model.AudioModel

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
        this.originalSongs = songs
        this.songs = songs
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
        if (currentIndex == songs.lastIndex) {
            return
        }
        currentIndex += 1
        play(songs[currentIndex])
        calculateStatus()
    }

    fun playPrevious() {
        if (currentIndex == 0) {
            return
        }
        currentIndex -= 1
        play(songs[currentIndex])
        calculateStatus()
    }

    fun repeat() {
        onRepeat = !onRepeat
        calculateStatus()
    }

    fun shuffle() {
        this.onShuffle = !onShuffle
        if (onShuffle) {
            this.songs = originalSongs.shuffled()
            play(songs[currentIndex])
        } else {
            this.songs = originalSongs
            play(originalSongs[currentIndex])
        }
        calculateStatus()
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