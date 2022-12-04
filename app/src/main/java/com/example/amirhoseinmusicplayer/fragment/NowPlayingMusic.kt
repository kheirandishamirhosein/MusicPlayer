package com.example.amirhoseinmusicplayer.fragment

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.activity.MusicPlayerActivity
import com.example.amirhoseinmusicplayer.databinding.FragmentNowPlayingMusicBinding
import com.example.amirhoseinmusicplayer.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.service.NotificationReceiver


class NowPlayingMusic : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingMusicBinding
        var musicPlayerActivity = MusicPlayerActivity()
        var notificationReceiver = NotificationReceiver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing_music, container, false)
        binding = FragmentNowPlayingMusicBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        Log.d("sdsdsd", "on Create View")

        //pause play
        binding.abPausePlay.setOnClickListener {
            Log.d("sdsdsd", "abPausePlay.setOnClickListener")
            if (MusicPlayerActivity.mediaPlayer.isPlaying) pauseNowMusic() else playNowMusic()
        }
        //next
        binding.abNext.setOnClickListener {
            Log.d("sdsdsd", "abNext.setOnClickListener")
            nextNowMusic()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("sdsdsd", "on resume")
        if (MusicPlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.tvNowPlayingSong.isSelected = true
            //image loading
            val image = getAlbumArt(MusicPlayerActivity.currentSong.path)
            Glide.with(this)
                .load(image)
                .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
                .into(binding.ivNowMusic)
            //MusicPlayerActivity.musicService!!.showNotification(R.drawable.ic_play)
            binding.tvNowPlayingSong.text = MusicPlayerActivity.currentSong.title
            if (MusicPlayerActivity.mediaPlayer.isPlaying)
                binding.abPausePlay.setIconResource(R.drawable.ic_pause)
            else binding.abPausePlay.setIconResource(R.drawable.ic_play)
        }
    }

    //play now music
    private fun playNowMusic() {
        Log.d("sdsdsd", "play Now Music")
        MusicPlayerActivity.mediaPlayer.start()
        binding.abPausePlay.setIconResource(R.drawable.ic_pause)
        MusicPlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
        MusicPlayerActivity.ivNext?.setIconResource(R.drawable.ic_pause)
    }

    //pause now music
    private fun pauseNowMusic() {
        Log.d("sdsdsd", "pause Now Music")
        MusicPlayerActivity.mediaPlayer.pause()
        binding.abPausePlay.setIconResource(R.drawable.ic_play)
        MusicPlayerActivity.musicService!!.showNotification(R.drawable.ic_play)
        MusicPlayerActivity.ivNext?.setIconResource(R.drawable.ic_play)
    }

    //next now music
    private fun nextNowMusic() {
        if (AudioMediaPlayer.currentIndex == MusicPlayerActivity.songsList.size - 1)
            return
        AudioMediaPlayer.currentIndex += 0
        MusicPlayerActivity.mediaPlayer.reset()
        //image loading
        val image = getAlbumArt(MusicPlayerActivity.currentSong.path)
        Glide.with(this)
            .load(image)
            .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
            .into(binding.ivNowMusic)
        //title song
        binding.tvNowPlayingSong.text = MusicPlayerActivity.currentSong.title
        MusicPlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
        playNowMusic()
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