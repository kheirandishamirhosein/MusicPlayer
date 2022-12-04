package com.example.amirhoseinmusicplayer.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.amirhoseinmusicplayer.ApplicationClass
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.activity.MusicPlayerActivity

class MusicService : Service() {
    private var myMusicBinder = MyMusicBinder()
    private lateinit var mediaSession: MediaSessionCompat
    private var musicPlayerActivity = MusicPlayerActivity()
    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My music")
        return myMusicBinder
    }

    inner class MyMusicBinder : Binder() {
        fun currentMusicService(): MusicService {
            return this@MusicService
        }
    }

    //show notification
    @SuppressLint("ResourceAsColor", "UnspecifiedImmutableFlag")
    fun showNotification(playPauseButton: Int) {
        //previous intent
        val previousIntent = Intent(baseContext, NotificationReceiver::class.java)
            .setAction(ApplicationClass.PREVIOUS)
        val previousPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        //next intent
        val nextIntent = Intent(baseContext, NotificationReceiver::class.java)
            .setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        //play intent
        val playIntent = Intent(baseContext, NotificationReceiver::class.java)
            .setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, playIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        //exit intent
        val exitIntent = Intent(baseContext, NotificationReceiver::class.java)
            .setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext, 777, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        //image notification
        val imgArt = getAlbumArt(MusicPlayerActivity.currentSong.path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(baseContext.resources, R.drawable.ic_music_list)
        }
        //notification
        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle(MusicPlayerActivity.currentSong.title)
            .setContentText(MusicPlayerActivity.currentSong.artist)
            .setSmallIcon(R.drawable.ic_music_note)
            //BitmapFactory.decodeResource(baseContext.resources,R.drawable.ic_music_list)
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
            .addAction(playPauseButton, "play", playPendingIntent)
            .addAction(R.drawable.ic_skip_next, "next", nextPendingIntent)
            .addAction(R.drawable.ic_exit_clear, "exit", exitPendingIntent)
            .build()
        startForeground(777, notification)
    }

    //image loading
    private fun getAlbumArt(uri: String): ByteArray? {
        val uriAlbumArt = MediaMetadataRetriever()
        uriAlbumArt.setDataSource(uri)
        val art = uriAlbumArt.embeddedPicture
        uriAlbumArt.release()
        return art
    }

}