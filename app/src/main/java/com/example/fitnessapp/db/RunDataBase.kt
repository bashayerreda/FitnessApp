package com.example.fitnessapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
@Database(
    entities = [RunData::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunningDao
}

