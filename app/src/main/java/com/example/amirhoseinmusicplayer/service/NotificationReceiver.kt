package com.example.amirhoseinmusicplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.ApplicationClass
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.activity.MusicPlayerActivity
import com.example.amirhoseinmusicplayer.fragment.NowPlayingMusic
import com.example.amirhoseinmusicplayer.mediaplayer.AudioMediaPlayer
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    private var musicPlayerActivity = MusicPlayerActivity()
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> previous(context!!)
            ApplicationClass.PLAY -> if (MusicPlayerActivity.mediaPlayer.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> nextMusic(context!!)
            ApplicationClass.EXIT -> {
                MusicPlayerActivity.musicService!!.stopForeground(true)
                MusicPlayerActivity.musicService = null
                exitProcess(1)
            }
        }
    }

    //play
    fun playMusic() {
        MusicPlayerActivity.playing = true
        MusicPlayerActivity.mediaPlayer.start()
        MusicPlayerActivity.musicService?.showNotification(R.drawable.ic_pause)
        MusicPlayerActivity.ivPausePlay?.setIconResource(R.drawable.ic_pause)
        NowPlayingMusic.binding.abPausePlay.setIconResource(R.drawable.ic_pause)
    }

    //pause
    fun pauseMusic() {
        MusicPlayerActivity.playing = false
        MusicPlayerActivity.mediaPlayer.pause()
        MusicPlayerActivity.musicService?.showNotification(R.drawable.ic_play)
        MusicPlayerActivity.ivPausePlay?.setIconResource(R.drawable.ic_play)
        NowPlayingMusic.binding.abPausePlay.setIconResource(R.drawable.ic_play)
    }

    //next
    fun nextMusic(context: Context) {
        if (AudioMediaPlayer.currentIndex == MusicPlayerActivity.songsList.size - 1)
            return
        AudioMediaPlayer.currentIndex += 0
        MusicPlayerActivity.mediaPlayer.reset()
        //image loading
        val image = getAlbumArt(MusicPlayerActivity.currentSong.path)
        Glide.with(context)
            .load(image)
            .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
            .into(NowPlayingMusic.binding.ivNowMusic)
        NowPlayingMusic.binding.tvNowPlayingSong.text = MusicPlayerActivity.currentSong.title
        playMusic()
    }

    //previous
    fun previous(context: Context) {
        if (AudioMediaPlayer.currentIndex == 0)
            return
        AudioMediaPlayer.currentIndex -= 2
        MusicPlayerActivity.mediaPlayer.reset()
        //image loading
        val image = getAlbumArt(MusicPlayerActivity.currentSong.path)
        Glide.with(context)
            .load(image)
            .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
            .into(NowPlayingMusic.binding.ivNowMusic)
        NowPlayingMusic.binding.tvNowPlayingSong.text = MusicPlayerActivity.currentSong.title
        playMusic()
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