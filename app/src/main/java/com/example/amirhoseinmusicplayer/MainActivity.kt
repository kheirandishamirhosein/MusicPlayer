package com.example.amirhoseinmusicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.amirhoseinmusicplayer.handler.MusicListHandler
import com.example.amirhoseinmusicplayer.data.MusicRepository


class MainActivity : AppCompatActivity() {
    private var recyclerViewMusic: RecyclerView? = null
    private lateinit var musicListAdapter: MusicListAdapter
    private val musicRepository by lazy { MusicRepository(this) }
    private val musicListHandler: MusicListHandler by lazy { MusicListHandler(this) }

    @SuppressLint("Recycle", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //request run time
        requestRunTimePermission()
        setContentView(R.layout.activity_main)

        recyclerViewMusic = findViewById(R.id.recycler_view_list_songs)
        val tvNoMusic: TextView = findViewById(R.id.tv_no_songs)
        val songsList = musicRepository.loadSongs()
        if (songsList.isEmpty()) {
            tvNoMusic.visibility = View.VISIBLE
        } else {
            musicListAdapter = MusicListAdapter(songsList, musicListHandler)
            musicListHandler.setAdapter(musicListAdapter)
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
                musicListHandler.search(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

}
