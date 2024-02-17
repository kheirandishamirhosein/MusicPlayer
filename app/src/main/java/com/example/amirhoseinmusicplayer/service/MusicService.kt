package com.example.amirhoseinmusicplayer.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.data.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.data.mediaplayer.PlayMode
import com.example.amirhoseinmusicplayer.data.mediaplayer.PlayerStatus
import com.example.amirhoseinmusicplayer.util.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {
    private lateinit var mediaSession: MediaSessionCompat
    @Inject lateinit var mediaPlayer: AudioMediaPlayer

    private val observer = object : Observer<PlayerStatus?> {
        override fun onChanged(status: PlayerStatus?) {
            status ?: return
            showNotification(status)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleAction(intent?.action)
        return START_NOT_STICKY
    }

    private fun handleAction(action: String?) {
        action ?: return
        when (action) {
            ACTION_START -> {
                createNotificationChannel()
                val status = mediaPlayer.status.value!!
                val notification = buildNotification(status)
                startForeground(NOTIF_ID, notification)
                mediaPlayer.status.observeForever(observer)
            }
            ACTION_STOP -> {
                mediaPlayer.status.removeObserver(observer)
                stopForeground(true)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "My music")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.status.removeObserver(observer)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun showNotification(status: PlayerStatus) {
        val notification = buildNotification(status)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, notification)
    }

    //show notification
    @SuppressLint("ResourceAsColor", "UnspecifiedImmutableFlag")
    fun buildNotification(status: PlayerStatus): Notification {
        //previous intent
        val previousIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(PREVIOUS)
        val previousPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, previousIntent, PendingIntent.FLAG_IMMUTABLE//PendingIntent.FLAG_UPDATE_CURRENT
        )
        //next intent
        val nextIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, nextIntent, PendingIntent.FLAG_IMMUTABLE//PendingIntent.FLAG_UPDATE_CURRENT
        )
        //play intent
        val playIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, playIntent, PendingIntent.FLAG_IMMUTABLE//PendingIntent.FLAG_UPDATE_CURRENT
        )
        //exit intent
        val exitIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, exitIntent, PendingIntent.FLAG_IMMUTABLE//PendingIntent.FLAG_UPDATE_CURRENT
        )
        //image notification
        val imgArt = ImageLoader.getAlbumArt(status.currentSong.path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(baseContext.resources, R.drawable.ic_music_list)
        }
        //notification
        val playIcon = if (status.mode == PlayMode.PLAY) R.drawable.ic_pause else R.drawable.ic_play
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(status.currentSong.title)
            .setContentText(status.currentSong.artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(image)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setColor(R.color.dark1)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_skip_previous, "previous", previousPendingIntent)
            .addAction(playIcon, "play", playPendingIntent)
            .addAction(R.drawable.ic_skip_next, "next", nextPendingIntent)
            .addAction(R.drawable.ic_exit_clear, "exit", exitPendingIntent)
            .build()

    }

    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "now playing song",
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationChannel.description = "this is a important channel for showing song!!"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val ACTION_START: String = "start_music_player_service"
        const val ACTION_STOP: String = "stop_music_player_service"
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
        const val NOTIF_ID = 777
    }

}