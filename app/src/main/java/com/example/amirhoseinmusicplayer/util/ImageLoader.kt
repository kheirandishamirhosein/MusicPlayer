package com.example.amirhoseinmusicplayer.util

import android.media.MediaMetadataRetriever

object ImageLoader {

     fun getAlbumArt(uri: String): ByteArray? {
        val uriAlbumArt = MediaMetadataRetriever()
        uriAlbumArt.setDataSource(uri)
        val art = uriAlbumArt.embeddedPicture
        uriAlbumArt.release()
        return art
    }
}