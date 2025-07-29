package com.example.mypdaviesapp.di

import android.content.Context
import androidx.room.Room
import com.example.mypdaviesapp.dao.BarcodeDao
import com.example.mypdaviesapp.dao.CleaningHistoryDao
import com.example.mypdaviesapp.dao.ClientDao
import com.example.mypdaviesapp.db.AppDatabase
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "carpet_cleaning_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideClientDao(database: AppDatabase): ClientDao = database.clientDao()

    @Provides
    fun provideBarcodeDao(database: AppDatabase): BarcodeDao = database.barcodeDao()

    @Provides
    fun provideCleaningHistoryDao(database: AppDatabase): CleaningHistoryDao = database.cleaningHistoryDao()
}