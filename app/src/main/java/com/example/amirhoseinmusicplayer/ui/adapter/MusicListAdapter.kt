package com.example.amirhoseinmusicplayer.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.model.AudioModel
import com.example.amirhoseinmusicplayer.util.ImageLoader
import java.util.*

class MusicListDiffCallBack(
    private val oldList: List<AudioModel>,
    private val newList: List<AudioModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}

class MusicListAdapter(
    private var songsList: List<AudioModel>,
    private val musicClickListener: MusicClickListener
) : RecyclerView.Adapter<MusicListAdapter.MusicListViewHolder>() {

    //update
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updateMusicList: List<AudioModel>) {
        val diffCallback = MusicListDiffCallBack(songsList, updateMusicList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        songsList = updateMusicList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MusicListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_list_music, parent, false)
        return MusicListViewHolder(itemView, musicClickListener)
    }

    override fun onBindViewHolder(holder: MusicListViewHolder, position: Int) {
        val item = songsList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    class MusicListViewHolder(view: View, musicClickListener: MusicClickListener) :
        RecyclerView.ViewHolder(view) {
        //init views
        private lateinit var audioModel: AudioModel
        private val tvMusicTitle: TextView = view.findViewById(R.id.tv_music_title)
        private val ivMenuMore: ImageView = view.findViewById(R.id.iv_menu_more)
        private val tvMusicArtist: TextView = view.findViewById(R.id.tv_music_artist)
        private val ivMusicImage: ImageView = view.findViewById(R.id.iv_icon_list_music)
        private val tvMusicTime: TextView = view.findViewById(R.id.tv_music_time)

        init {
            itemView.setOnClickListener { musicClickListener.onMusicClick(adapterPosition) }
            ivMenuMore.setOnClickListener {
                musicClickListener.onMusicMenuItemClick(
                    audioModel,
                    it
                )
            }
        }

        fun bind(audioModel: AudioModel) {
            this.audioModel = audioModel
            tvMusicTitle.text = audioModel.title
            tvMusicArtist.text = audioModel.artist
            tvMusicTime.text = audioModel.formattedDuration
            val image = ImageLoader.getAlbumArt(audioModel.path)
            Glide.with(ivMusicImage)
                .load(image)
                .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
                .into(ivMusicImage)
        }

    }
}

interface MusicClickListener {

    fun onMusicClick(musicIndex: Int)

    fun onMusicMenuItemClick(audioModel: AudioModel, itemMenu: View)
}