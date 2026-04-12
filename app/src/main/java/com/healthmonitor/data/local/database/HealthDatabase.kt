package com.healthmonitor.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.healthmonitor.data.local.dao.HealthDao
import com.healthmonitor.data.local.entity.HealthAlertEntity
import com.healthmonitor.data.local.entity.HealthReadingEntity

@Database(
    entities = [HealthReadingEntity::class, HealthAlertEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao
}
