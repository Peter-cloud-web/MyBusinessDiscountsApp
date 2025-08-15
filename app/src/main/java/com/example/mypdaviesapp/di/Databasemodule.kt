package com.example.mypdaviesapp.di

import com.example.mypdaviesapp.repo.SyncManager
import android.content.Context
import androidx.room.Room
import com.example.mypdaviesapp.dao.AppMetadataDao
import com.example.mypdaviesapp.dao.BarcodeDao
import com.example.mypdaviesapp.dao.CleaningHistoryDao
import com.example.mypdaviesapp.dao.ClientDao
import com.example.mypdaviesapp.db.AppDatabase
import com.example.mypdaviesapp.repo.CarpetCleaningRepository
import com.example.mypdaviesapp.repo.MetadataManager
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideMetadataDao(database: AppDatabase): AppMetadataDao {
        return database.appMetadataDao()
    }

    @Provides
    @Singleton
    fun provideMetadataManager(
        metadataDao: AppMetadataDao,
        firestore: FirebaseFirestore
    ): MetadataManager {
        return MetadataManager(metadataDao, firestore)
    }

    @Provides
    fun provideClientDao(database: AppDatabase): ClientDao = database.clientDao()

    @Provides
    fun provideBarcodeDao(database: AppDatabase): BarcodeDao = database.barcodeDao()

    @Provides
    fun provideCleaningHistoryDao(database: AppDatabase): CleaningHistoryDao = database.cleaningHistoryDao()

    @Provides
    @Singleton
    fun provideSyncManager(
        repository: CarpetCleaningRepository,
        metadataManager: MetadataManager
    ): SyncManager {
        return SyncManager(repository, metadataManager)
    }
}