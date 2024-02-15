package com.example.amirhoseinmusicplayer.di

import com.example.amirhoseinmusicplayer.data.mediaplayer.AudioMediaPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaPlayerModule {

    @Provides
    @Singleton
    fun provideAudioMediaPlayer(): AudioMediaPlayer {
        return AudioMediaPlayer
    }

}