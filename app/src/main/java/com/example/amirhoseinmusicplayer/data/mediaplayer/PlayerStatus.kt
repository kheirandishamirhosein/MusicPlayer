package com.example.amirhoseinmusicplayer.data.mediaplayer

import com.example.amirhoseinmusicplayer.model.AudioModel

data class PlayerStatus(
    val mode: PlayMode = PlayMode.STOP,
    val currentPosition: Int,
    val onRepeat: Boolean,
    val onShuffle: Boolean,
    val currentSong: AudioModel
)

enum class PlayMode {
    PLAY,
    PAUSE,
    STOP
}
