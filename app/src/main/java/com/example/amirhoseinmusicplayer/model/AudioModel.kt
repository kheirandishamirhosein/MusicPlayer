package com.example.amirhoseinmusicplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//data class model
@Parcelize
data class AudioModel(
    val path: String,
    val title: String,
    val duration: String,
    val id: String,
    val artist: String,
    val image: String
) : Parcelable


