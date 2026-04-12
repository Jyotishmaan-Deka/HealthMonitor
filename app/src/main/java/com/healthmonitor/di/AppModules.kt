package com.healthmonitor.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthmonitor.data.local.database.HealthDatabase
import com.healthmonitor.data.local.dao.HealthDao
import com.healthmonitor.data.remote.firebase.FirebaseService
import com.healthmonitor.data.repository.HealthRepositoryImpl
import com.healthmonitor.domain.repository.HealthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ── Database Module ────────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHealthDatabase(@ApplicationContext context: Context): HealthDatabase =
        Room.databaseBuilder(context, HealthDatabase::class.java, "health_monitor_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideHealthDao(db: HealthDatabase): HealthDao = db.healthDao()
}

// ── Firebase Module ────────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

// ── Repository Module ─────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository
}
