package com.example.amirhoseinmusicplayer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.databinding.FragmentNowPlayingMusicBinding
import com.example.amirhoseinmusicplayer.data.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.data.mediaplayer.PlayMode
import com.example.amirhoseinmusicplayer.data.mediaplayer.PlayerStatus
import com.example.amirhoseinmusicplayer.model.AudioModel
import com.example.amirhoseinmusicplayer.util.ImageLoader

class NowPlayingMusic : Fragment() {

    private lateinit var binding: FragmentNowPlayingMusicBinding
    private val mediaPlayer = AudioMediaPlayer
    private var prevSong: AudioModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing_music, container, false)
        binding = FragmentNowPlayingMusicBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        //pause play
        binding.abPausePlay.setOnClickListener {
            mediaPlayer.toggle()
        }
        //next
        binding.abNext.setOnClickListener {
            mediaPlayer.playNext()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaPlayer.status.observe(viewLifecycleOwner) { showPlayerStatus(it) }
    }

    private fun showPlayerStatus(status: PlayerStatus) = with(binding) {
        root.visibility = if (status.mode != PlayMode.STOP) View.VISIBLE else View.GONE
        abPausePlay.setIconResource(if (status.mode == PlayMode.PLAY) R.drawable.ic_pause else R.drawable.ic_play)
        if (prevSong != status.currentSong) {
            tvNowPlayingSong.text = status.currentSong.title
            val image = ImageLoader.getAlbumArt(status.currentSong.path)
            Glide.with(this@NowPlayingMusic)
                .load(image)
                .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
                .into(ivNowMusic)
        }
        prevSong = status.currentSong
    }

}