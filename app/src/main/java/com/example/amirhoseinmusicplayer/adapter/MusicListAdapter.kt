package com.example.amirhoseinmusicplayer.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.activity.MusicPlayerActivity
import com.example.amirhoseinmusicplayer.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.model.AudioModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.lang.Long
import java.util.*
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Int
import kotlin.String


class MusicListAdapter(
    private var songsList: ArrayList<AudioModel>,
    private var context: Context
) : RecyclerView.Adapter<MusicListAdapter.MusicListViewHolder>() {

    class MusicListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //init views
        var tvMusicTitle: TextView = view.findViewById(R.id.tv_music_title)
        var ivMenuMore: ImageView = view.findViewById(R.id.iv_menu_more)
        var tvMusicArtist: TextView = view.findViewById(R.id.tv_music_artist)
        var ivMusicImage: ImageView = view.findViewById(R.id.iv_icon_list_music)
        var tvMusicTime: TextView = view.findViewById(R.id.tv_music_time)

        //bind title music in list
        fun bindMusicTitle(musicTitle: AudioModel) {
            tvMusicTitle.text = musicTitle.title
        }

        //bind artist music in list
        fun bindMusicArtist(musicArtist: AudioModel) {
            tvMusicArtist.text = musicArtist.artist
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MusicListAdapter.MusicListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_list_music, parent, false)
        return MusicListAdapter.MusicListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MusicListAdapter.MusicListViewHolder, position: Int) {
        val item = songsList[position]
        holder.bindMusicTitle(item)
        holder.bindMusicArtist(item)
        holder.tvMusicTime.text = formatDuration((item.duration).toInt())
        //loading image
        val image = getAlbumArt(songsList[position].path)
        Glide.with(context)
            .load(image)
            .apply(RequestOptions().placeholder(R.drawable.ic_music_list).centerCrop())
            .into(holder.ivMusicImage)
        //intent to music player activity
        holder.itemView.setOnClickListener {
            //navigate to another activity
            AudioMediaPlayer.getInstance().start()
            AudioMediaPlayer.currentIndex = position
            val intent = Intent(context, MusicPlayerActivity::class.java)
            intent.putExtra("LIST", songsList)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        holder.ivMenuMore.setOnClickListener { itemMenu ->
            val menuItemListRecyclerView = PopupMenu(context, itemMenu)
            menuItemListRecyclerView.inflate(R.menu.menu_item_recyclerview)
            menuItemListRecyclerView.show()
            menuItemListRecyclerView.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete -> {
                        val builder = AlertDialog.Builder(context)
                            .setTitle("Are you sure to delete this music?")
                        builder.setPositiveButton("Yes") { _, _ ->
                            fileDeleted(position, itemMenu)
                            Toast.makeText(context, "Delete Clicked!!", Toast.LENGTH_SHORT).show()
                        }
                        builder.setNegativeButton("No") { _, _ ->
                            Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show()
                        }
                        builder.show()
                    }
                    R.id.share -> {
                        fileShare(position, itemMenu)
                        Toast.makeText(context, "Share Clicked!!", Toast.LENGTH_SHORT).show()
                    }

                }
                return@setOnMenuItemClickListener true
            }
        }
        //sort list songs
        songsList.sortBy {
            it.title

        }

    }

    override fun getItemCount(): Int {
        return songsList.size

    }

    //delete file
    private fun fileDeleted(position: Int, view: View) {
        val uri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            Long.parseLong(songsList[position].id)
        )
        val deleteFile = File(songsList[position].path)
        val deleted: Boolean = deleteFile.delete() // delete your file
        if (deleted) {
            context.contentResolver.delete(uri, null, null)
            songsList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, songsList.size)
        } else {
            Snackbar.make(view, "Can't be Deleted", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    //share file
    private fun fileShare(position: Int, view: View) {
        val uri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            Long.parseLong(songsList[position].id)
        )
        val shareFile = File(songsList[position].path)
        val share: Boolean = shareFile.exists()
        if (share) {
            val shareAudio = Intent()
            shareAudio.action = Intent.ACTION_SEND
            shareAudio.type = "audio/*"
            shareAudio.putExtra(Intent.EXTRA_STREAM, uri)
            context.startActivity(Intent.createChooser(shareAudio, "Sharing Music File"))
        } else {
            Snackbar.make(view, "Can't be Shared", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    //update
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updateMusicList: ArrayList<AudioModel>) {
        songsList = ArrayList()
        songsList.addAll(updateMusicList)
        notifyDataSetChanged()
    }

    //image loading
    private fun getAlbumArt(uri: String): ByteArray? {
        val uriAlbumArt = MediaMetadataRetriever()
        uriAlbumArt.setDataSource(uri)
        val art = uriAlbumArt.embeddedPicture
        uriAlbumArt.release()
        return art
    }

    //format duration for music time
    private fun formatDuration(duration: Int): String {
        var seconds: Int = (duration / 1000)
        val minutes: Int = seconds / 60
        seconds %= 60
        return String.format(Locale.ENGLISH, "%02d", minutes) +
                ":" +
                String.format(Locale.ENGLISH, "%02d", seconds)
    }
}