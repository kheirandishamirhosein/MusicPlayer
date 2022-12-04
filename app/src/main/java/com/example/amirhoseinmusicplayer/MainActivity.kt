package com.example.amirhoseinmusicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amirhoseinmusicplayer.adapter.MusicListAdapter
import com.example.amirhoseinmusicplayer.model.AudioModel
import java.io.File


class MainActivity : AppCompatActivity() {

    //8
    private var songsList: ArrayList<AudioModel> = ArrayList()
    private var recyclerViewMusic: RecyclerView? = null

    //for search view
    private lateinit var musicListSearch: ArrayList<AudioModel>
    private lateinit var musicListAdapter: MusicListAdapter

    @SuppressLint("Recycle", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //request run time
        requestRunTimePermission()
        setContentView(R.layout.activity_main)

        //1
        recyclerViewMusic = findViewById(R.id.recycler_view_list_songs)
        val tvNoMusic: TextView = findViewById(R.id.tv_no_songs)

        //5
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA, //for path
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )
        //6
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        //7
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null
        )

        //9
        while (cursor!!.moveToNext()) {
            val songData =
                AudioModel(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
                )
            if (File(songData.path).exists()) songsList.add(songData)
        }

        //10
        if (songsList.size == 0) {
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
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase().trim()
                    for (song in songsList) {
                        if (song.title.lowercase().contains(userInput)
                            or
                            song.artist.lowercase().contains(userInput)
                        ) {
                            musicListSearch.add(song)
                            musicListAdapter.updateList(musicListSearch)
                        }
                    }
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

}
