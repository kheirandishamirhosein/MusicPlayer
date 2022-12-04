package com.example.amirhoseinmusicplayer.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.fragment.NowPlayingMusic
import com.example.amirhoseinmusicplayer.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.model.AudioModel
import com.example.amirhoseinmusicplayer.service.MusicService
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.IOException
import java.util.*


class MusicPlayerActivity : AppCompatActivity(), ServiceConnection {


    //3
    companion object {
        //1
        //The first and upper part
        @SuppressLint("StaticFieldLeak")
        var tvTitle: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var tvArtist: TextView? = null

        // The second and middle part
        @SuppressLint("StaticFieldLeak")
        var ivSongIcon: ImageView? = null

        //The third and lower part
        @SuppressLint("StaticFieldLeak")
        var tvCurrentTime: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var tvTotalTime: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var seekBar: SeekBar? = null
        var ivPausePlay: ExtendedFloatingActionButton? = null
        var ivNext: ExtendedFloatingActionButton? = null
        var ivPrevious: ExtendedFloatingActionButton? = null

        @SuppressLint("StaticFieldLeak")
        var ivRepeatSong: ImageView? = null

        @SuppressLint("StaticFieldLeak")
        var ivShuffleSong: ImageView? = null

        //object and instance Audio model
        var songsList: ArrayList<AudioModel> = ArrayList()
        lateinit var currentSong: AudioModel
        var mediaPlayer: MediaPlayer = AudioMediaPlayer.getInstance()
        private var itemSongsList = emptyList<AudioModel>()
        var playing = true
        var shuffleFlag = false
        var repeatFlag = false

        //music service
        var musicService: MusicService? = null

        //now play music
        var nowPlayingMusic = NowPlayingMusic()
    }

    //onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        //intent to music service
        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, this, BIND_AUTO_CREATE)
        //back activity
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // init function
        initViews()
        //init starting service
        startingService()
        //set on click listener button
        ivShuffleSong?.setOnClickListener { shuffleSong() }
        ivRepeatSong?.setOnClickListener { repeatSong() }
        //5
        songsList = intent.getSerializableExtra("LIST") as ArrayList<AudioModel>
        //set resources init
        setResourcesWithMusic()
        //runnable, run on ui thread
        runOnUiThread(object : Runnable {
            private val mHandler = Handler()
            override fun run() {
                if (mediaPlayer != null) {
                    seekBar?.progress = mediaPlayer.currentPosition
                    tvCurrentTime?.text = formatDuration(mediaPlayer.currentPosition)
                    if (mediaPlayer.isPlaying) {
                        ivPausePlay?.setIconResource(R.drawable.ic_pause)
                        musicService?.showNotification(R.drawable.ic_pause)
                        // NowPlayingMusic.binding.abPausePlay.setIconResource(R.drawable.ic_pause)
                    } else {
                        ivPausePlay?.setIconResource(R.drawable.ic_play)
                        musicService?.showNotification(R.drawable.ic_play)
                        // NowPlayingMusic.binding.abPausePlay.setIconResource(R.drawable.ic_play)
                    }
                }
                mHandler.postDelayed(this, 100)
            }
        })
        //seekBar setting
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

    // init views
    private fun initViews() {
        //4
        //The first and upper part
        tvTitle = findViewById(R.id.tv_songs_Title)
        tvArtist = findViewById(R.id.tv_songs_Artist)
        tvTitle?.isSelected = true
        // The second and middle part
        ivSongIcon = findViewById(R.id.iv_music_icon_big)
        //The third and lower part
        ivNext = findViewById(R.id.iv_next)
        ivPrevious = findViewById(R.id.iv_previous)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        ivPausePlay = findViewById(R.id.iv_pause_play)
        ivRepeatSong = findViewById(R.id.iv_repeat)
        ivShuffleSong = findViewById(R.id.iv_shuffle)
        seekBar = findViewById(R.id.seekBar)
    }

    //6 set resources
    @SuppressLint("CheckResult")
    fun setResourcesWithMusic() {
        currentSong = songsList[AudioMediaPlayer.currentIndex]
        //8 -> calls title , artist , image
        tvTitle?.text = currentSong.title
        tvArtist?.text = currentSong.artist
        //load image art
        if (!this.isFinishing) {
            val image = getAlbumArt(currentSong.path)
            Glide.with(this)
                .load(image)
                .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
                .into(ivSongIcon!!)
        }
        //total time
        tvTotalTime?.text = formatDuration((currentSong.duration).toInt())
        //listener buttons
        ivPausePlay?.setOnClickListener { pausePlayMusic() }
        ivNext?.setOnClickListener { nextSong() }
        ivPrevious?.setOnClickListener { previousSong() }
        playMusic()
    }

    //image loading
    fun getAlbumArt(uri: String): ByteArray? {
        val uriAlbumArt = MediaMetadataRetriever()
        uriAlbumArt.setDataSource(uri)
        val art = uriAlbumArt.embeddedPicture
        uriAlbumArt.release()
        return art
    }

    //9 play
    fun playMusic() {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(currentSong.path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            seekBar?.progress = 0
            seekBar?.max = mediaPlayer.duration
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //when music is end and repeat
        mediaPlayer.setOnCompletionListener {
            if (repeatFlag) {
                playMusic()
            } else {
                mediaPlayer.seekTo(0)
                nextSong()
            }
        }
    }

    //pause play music
    private fun pausePlayMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
    }

    //next
    private fun nextSong() {
        if (AudioMediaPlayer.currentIndex == songsList.size - 1)
            return
        AudioMediaPlayer.currentIndex += 1
        mediaPlayer.reset()
        setResourcesWithMusic()
    }

    //previous
    private fun previousSong() {
        if (AudioMediaPlayer.currentIndex == 0)
            return
        AudioMediaPlayer.currentIndex -= 1
        mediaPlayer.reset()
        setResourcesWithMusic()
    }

    //shuffle
    private fun shuffleSong() {
        if (shuffleFlag) {
            ivShuffleSong?.setImageResource(R.drawable.ic_shuffle_off)
            itemSongsList = songsList
        } else {
            ivShuffleSong?.setImageResource(R.drawable.ic_shuffle_on)
            songsList.shuffle()
            songsList.toArray()
        }
        shuffleFlag = !shuffleFlag
    }

    //repeat
    private fun repeatSong() {
        if (repeatFlag) {
            ivRepeatSong?.setImageResource(R.drawable.ic_repeat_off)
        } else {
            ivRepeatSong?.setImageResource(R.drawable.ic_repeat_on)
        }
        repeatFlag = !repeatFlag
    }

    //format duration
    fun formatDuration(duration: Int): String {
        var seconds: Int = (duration / 1000)
        val minutes: Int = seconds / 60
        seconds %= 60
        return String.format(Locale.ENGLISH, "%02d", minutes) +
                ":" +
                String.format(Locale.ENGLISH, "%02d", seconds)
    }

    //for service connection
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binderService = service as MusicService.MyMusicBinder
        musicService = binderService.currentMusicService()
        playMusic()
        musicService!!.showNotification(R.drawable.ic_pause)
    }

    //for service disconnected
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    //for starting service
    private fun startingService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
    }

}
