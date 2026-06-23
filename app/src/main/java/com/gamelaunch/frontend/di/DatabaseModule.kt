package com.gamelaunch.frontend.di

import android.content.Context
import androidx.room.Room
import com.gamelaunch.frontend.data.db.AppDatabase
import com.gamelaunch.frontend.data.db.dao.EmulatorMappingDao
import com.gamelaunch.frontend.data.db.dao.GameDao
import com.gamelaunch.frontend.data.db.dao.GameMediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGameDao(db: AppDatabase): GameDao = db.gameDao()

    @Provides
    fun provideGameMediaDao(db: AppDatabase): GameMediaDao = db.gameMediaDao()

    @Provides
    fun provideEmulatorMappingDao(db: AppDatabase): EmulatorMappingDao = db.emulatorMappingDao()
}
