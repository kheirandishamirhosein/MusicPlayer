package com.example.amirhoseinmusicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amirhoseinmusicplayer.activity.MusicPlayerActivity
import com.example.amirhoseinmusicplayer.adapter.MusicClickListener
import com.example.amirhoseinmusicplayer.adapter.MusicListAdapter
import com.example.amirhoseinmusicplayer.data.MusicRepository
import com.example.amirhoseinmusicplayer.mediaplayer.AudioMediaPlayer
import com.example.amirhoseinmusicplayer.model.AudioModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.lang.Long


class MainActivity : AppCompatActivity(), MusicClickListener {
    private var recyclerViewMusic: RecyclerView? = null

    //for search view
    private lateinit var musicListAdapter: MusicListAdapter
    private val musicRepository by lazy { MusicRepository(this) }

    @SuppressLint("Recycle", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //request run time
        requestRunTimePermission()
        setContentView(R.layout.activity_main)

        //1
        recyclerViewMusic = findViewById(R.id.recycler_view_list_songs)
        val tvNoMusic: TextView = findViewById(R.id.tv_no_songs)

        val songsList = musicRepository.loadSongs()
        if (songsList.isEmpty()) {
            tvNoMusic.visibility = View.VISIBLE
        } else {
            //recyclerview
            musicListAdapter = MusicListAdapter(songsList, this)
            recyclerViewMusic?.layoutManager = LinearLayoutManager(this)
            recyclerViewMusic?.adapter = musicListAdapter
        }

    }

    /************ request Permission ***********/
    //for request permission
    private fun requestRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                777
            )
        }

    }

    //
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 777) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            else
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    777
                )
        }
    }

    //search
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sreach_view, menu)
        val searchView =
            menu.findItem(R.id.search)?.actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchResult = musicRepository.search(newText ?: "")
                musicListAdapter.updateList(searchResult)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMusicClick(musicIndex: Int) {
        //navigate to another activity
        AudioMediaPlayer.startPlaying(musicRepository.loadSongs(), musicIndex)
        val intent = Intent(this, MusicPlayerActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onMusicMenuItemClick(audioModel: AudioModel, itemMenu: View) {
        val menuItemListRecyclerView = PopupMenu(this, itemMenu)
        menuItemListRecyclerView.inflate(R.menu.menu_item_recyclerview)
        menuItemListRecyclerView.show()
        menuItemListRecyclerView.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Are you sure to delete this music?")
                    builder.setPositiveButton("Yes") { _, _ ->
                        fileDeleted(audioModel, itemMenu)
                        Toast.makeText(this, "Delete Clicked!!", Toast.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No") { _, _ ->
                        Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
                    }
                    builder.show()
                }
                R.id.share -> {
                    fileShare(audioModel, itemMenu)
                    Toast.makeText(this, "Share Clicked!!", Toast.LENGTH_SHORT).show()
                }

            }
            return@setOnMenuItemClickListener true
        }
    }

    //delete file
    private fun fileDeleted(audioModel: AudioModel, itemMenu: View) {
        val deleted = musicRepository.removeSong(audioModel)
        if (deleted) {
            musicListAdapter.updateList(musicRepository.loadSongs())
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
            startActivity(Intent.createChooser(shareAudio, "Sharing Music File"))
        } else {
            Snackbar.make(view, "Can't be Shared", Snackbar.LENGTH_LONG)
                .show()
        }
    }
}
