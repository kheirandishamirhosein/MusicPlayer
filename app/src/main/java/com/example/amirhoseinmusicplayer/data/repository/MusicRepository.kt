package com.example.amirhoseinmusicplayer.data.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.amirhoseinmusicplayer.model.AudioModel
import com.example.amirhoseinmusicplayer.util.DurationFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.lang.Exception
import java.lang.Long
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.arrayOf

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var songsList: List<AudioModel>? = null

    fun loadSongs(): List<AudioModel> {
        if (songsList != null) return songsList!!

        val songsList = mutableListOf<AudioModel>()
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
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null
        )

        //9
        while (cursor!!.moveToNext()) {
            val duration = cursor.getInt(2)
            val songData = AudioModel(
                path = cursor.getString(0),
                title = cursor.getString(1),
                duration = duration,
                id = cursor.getString(3),
                artist = cursor.getString(4),
                image = cursor.getString(5),
                formattedDuration = formatDuration(duration)
            )
            if (File(songData.path).exists()) songsList.add(songData)
        }
        songsList.sortBy { it.title }
        this.songsList = songsList
        return songsList
    }

    //format duration for music time
    private fun formatDuration(duration: Int): String {
        return DurationFormatter.formatDuration(duration)
    }

    fun removeSong(audioModel: AudioModel): Boolean {
        try {
            val uri: Uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(audioModel.id)
            )
            val deleteFile = File(audioModel.path)
            val deleted: Boolean = deleteFile.delete() // delete your file
            context.contentResolver.delete(uri, null, null)
            val list = songsList ?: return deleted
            songsList = list.minus(audioModel)
            return deleted
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e("khkhkh", "${ex.message}")
            return false
        }
    }

    fun search(query: String): List<AudioModel> {
        val allSongs = songsList ?: return emptyList()
        val userInput = query.lowercase().trim()
        if (userInput.isEmpty()) {
            return allSongs
        }
        return allSongs.filter {
            it.title.lowercase().contains(userInput) || it.artist.lowercase().contains(userInput)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var repository: MusicRepository? = null

        fun getInstance(context: Context): MusicRepository {
            if (repository == null) {
                repository = MusicRepository(context)
            }
            return repository!!
        }
    }
}