package com.example.amirhoseinmusicplayer.mediaplayer

import android.media.MediaPlayer

object AudioMediaPlayer : MediaPlayer() {

    private var mediaPlayer = AudioMediaPlayer
    var currentIndex = -1
    fun getInstance(): AudioMediaPlayer {
        return mediaPlayer
    }

}