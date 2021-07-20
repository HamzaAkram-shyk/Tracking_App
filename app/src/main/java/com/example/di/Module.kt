package com.example.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import com.example.localDb.RunDao
import com.example.localDb.RunDatabase
import com.example.stepcounterapp.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Singleton
    @Provides
    fun provideRoomDb(@ApplicationContext app: Context): RunDatabase {
        return Room.databaseBuilder(
            app,
            RunDatabase::class.java,
            RunDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideRunDao(database: RunDatabase): RunDao {
        return database.getRunDao()
    }



}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainActivityContext