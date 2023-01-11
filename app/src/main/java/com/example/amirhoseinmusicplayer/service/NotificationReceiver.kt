package com.example.amirhoseinmusicplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.amirhoseinmusicplayer.mediaplayer.AudioMediaPlayer

class NotificationReceiver : BroadcastReceiver() {
    private val mediaPlayer: AudioMediaPlayer = AudioMediaPlayer

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            MusicService.PREVIOUS -> mediaPlayer.playPrevious()
            MusicService.PLAY -> mediaPlayer.toggle()
            MusicService.NEXT -> mediaPlayer.playNext()
            MusicService.EXIT -> {
                mediaPlayer.stop()
                val serviceIntent = Intent(context, MusicService::class.java)
                serviceIntent.action = MusicService.ACTION_STOP
                context?.startService(serviceIntent)
            }
        }
    }
}