package com.example.amirhoseinmusicplayer.activity

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.data.MusicRepository
import com.example.amirhoseinmusicplayer.databinding.ActivityMusicPlayerBinding
import com.example.amirhoseinmusicplayer.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.mediaplayer.PlayMode
import com.example.amirhoseinmusicplayer.mediaplayer.PlayerStatus
import com.example.amirhoseinmusicplayer.service.MusicService
import com.example.amirhoseinmusicplayer.util.DurationFormatter

class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusicPlayerBinding
    private var mediaPlayer: AudioMediaPlayer = AudioMediaPlayer
    private var previousStatus: PlayerStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //call Repository
        val musicRepository = MusicRepository(this)
        //back activity
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //init starting service
        initViews()
        mediaPlayer.status.observe(this) { showPlayerStatus(it) }
        startService()
    }

    private fun initViews() {
        binding.ivShuffle.setOnClickListener { mediaPlayer.shuffle() }
        binding.ivRepeat.setOnClickListener { mediaPlayer.repeat() }
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

    //image loading
    private fun getAlbumArt(uri: String): ByteArray? {
        val uriAlbumArt = MediaMetadataRetriever()
        uriAlbumArt.setDataSource(uri)
        val art = uriAlbumArt.embeddedPicture
        uriAlbumArt.release()
        return art
    }

    private fun showPlayerStatus(newStatus: PlayerStatus) = with(binding) {
        ivShuffle.setImageResource(if (newStatus.onShuffle) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle_off)
        ivRepeat.setImageResource(if (newStatus.onRepeat) R.drawable.ic_repeat_on else R.drawable.ic_repeat_off)
        if (newStatus.currentSong != previousStatus?.currentSong) {
            tvSongsTitle.text = newStatus.currentSong.title
            tvSongsArtist.text = newStatus.currentSong.artist
            tvTotalTime.text = newStatus.currentSong.formattedDuration
            val image = getAlbumArt(newStatus.currentSong.path)
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
