package com.example.amirhoseinmusicplayer.data.handler

import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.example.amirhoseinmusicplayer.R
import com.example.amirhoseinmusicplayer.ui.activity.MusicPlayerActivity
import com.example.amirhoseinmusicplayer.ui.adapter.MusicClickListener
import com.example.amirhoseinmusicplayer.ui.adapter.MusicListAdapter
import com.example.amirhoseinmusicplayer.data.repository.MusicRepository
import com.example.amirhoseinmusicplayer.data.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.model.AudioModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.lang.Long
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Boolean
import kotlin.Int


  @Singleton
  class MusicListHandler @Inject constructor(
    private val context: Context,
    private val musicRepository: MusicRepository
  ): MusicClickListener {

    private lateinit var musicListAdapter: MusicListAdapter
    private var playList: List<AudioModel> = musicRepository.loadSongs()

    override fun onMusicClick(musicIndex: Int) {
        //navigate to another activity (intent)
        AudioMediaPlayer.startPlaying(playList, musicIndex)
        val intent = Intent(context, MusicPlayerActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun onMusicMenuItemClick(audioModel: AudioModel, itemMenu: View) {
        //در قسمت ایجاد PopupMenu
        val popupMenu = PopupMenu(itemMenu.context, itemMenu)
        popupMenu.menuInflater.inflate(R.menu.menu_item_recyclerview, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    val builder = AlertDialog.Builder(itemMenu.context)
                    builder.setTitle("Are you sure to delete this music?")
                    builder.setPositiveButton("Yes") { _, _ ->
                        fileDeleted(audioModel, itemMenu)
                        Toast.makeText(itemMenu.context, "Delete Clicked!!", Toast.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No") { _, _ ->
                        Toast.makeText(itemMenu.context, "Cancel", Toast.LENGTH_SHORT).show()
                    }
                    builder.show()
                }
                R.id.share -> {
                    fileShare(audioModel, itemMenu)
                    Toast.makeText(itemMenu.context, "Share Clicked!!", Toast.LENGTH_SHORT).show()
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()

    }

    //delete file
    private fun fileDeleted(audioModel: AudioModel, itemMenu: View) {
        val deleted = musicRepository.removeSong(audioModel)
        if (deleted) {
            updatePlayList(playList - audioModel)
        } else {
            Snackbar.make(itemMenu, "Can't be Deleted", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    //share file
    private fun fileShare(audioModel: AudioModel, view: View) {
        val uri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            Long.parseLong(audioModel.id)
        )
        val shareFile = File(audioModel.path)
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

    private fun updatePlayList(newPlayList: List<AudioModel>) {
        this.playList = newPlayList
        musicListAdapter.updateList(playList)
    }

    fun setAdapter(musicListAdapter: MusicListAdapter) {
        this.musicListAdapter = musicListAdapter
    }

    fun search(query: String?) {
        val searchResult = musicRepository.search(query ?: "")
        updatePlayList(searchResult)
    }

}