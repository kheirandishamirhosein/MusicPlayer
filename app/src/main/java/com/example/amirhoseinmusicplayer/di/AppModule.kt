package com.example.amirhoseinmusicplayer.di

import android.app.Application
import android.content.Context
import com.example.amirhoseinmusicplayer.data.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideMusicRepository(@ApplicationContext context: Context): MusicRepository {
        return MusicRepository(context)
    }

}