package com.example.amirhoseinmusicplayer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.data.repository.MusicRepository
import com.example.amirhoseinmusicplayer.databinding.ActivityMusicPlayerBinding
import com.example.amirhoseinmusicplayer.data.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.data.mediaplayer.PlayMode
import com.example.amirhoseinmusicplayer.data.mediaplayer.PlayerStatus
import com.example.amirhoseinmusicplayer.service.MusicService
import com.example.amirhoseinmusicplayer.util.DurationFormatter
import com.example.amirhoseinmusicplayer.util.ImageLoader
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusicPlayerBinding
    private var mediaPlayer: AudioMediaPlayer = AudioMediaPlayer
    private var previousStatus: PlayerStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //back activity
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //init starting service
        initViews()
        mediaPlayer.status.observe(this) { showPlayerStatus(it) }
        startService()
    }

    private fun initViews() {
        binding.ivShuffle.setOnClickListener { mediaPlayer.shuffle(this) }
        binding.ivRepeat.setOnClickListener { mediaPlayer.repeat(this) }
        binding.ivPausePlay.setOnClickListener { mediaPlayer.toggle() }
        binding.ivNext.setOnClickListener { mediaPlayer.playNext() }
        binding.ivPrevious.setOnClickListener { mediaPlayer.playPrevious() }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBarObject: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    private fun showPlayerStatus(newStatus: PlayerStatus) = with(binding) {
        ivShuffle.setImageResource(if (newStatus.onShuffle) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle_off)
        ivRepeat.setImageResource(if (newStatus.onRepeat) R.drawable.ic_repeat_on else R.drawable.ic_repeat_off)
        if (newStatus.currentSong != previousStatus?.currentSong) {
            tvSongsTitle.text = newStatus.currentSong.title
            tvSongsArtist.text = newStatus.currentSong.artist
            tvTotalTime.text = newStatus.currentSong.formattedDuration
            val image = ImageLoader.getAlbumArt(newStatus.currentSong.path)
            Glide.with(this@MusicPlayerActivity)
                .load(image)
                .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
                .into(ivMusicIconBig)
            seekBar.max = mediaPlayer.duration
        }
        ivPausePlay.setIconResource(if (newStatus.mode == PlayMode.PLAY) R.drawable.ic_pause else R.drawable.ic_play)
        seekBar.progress = mediaPlayer.currentPosition
        tvCurrentTime.text = DurationFormatter.formatDuration(mediaPlayer.currentPosition)
        previousStatus = newStatus
    }

    private fun startService() {
        val intent = Intent(this, MusicService::class.java)
        intent.action = MusicService.ACTION_START
        startService(intent)
    }
}
