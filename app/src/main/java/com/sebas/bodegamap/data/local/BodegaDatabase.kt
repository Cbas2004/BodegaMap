package com.sebas.bodegamap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BodegaEntity::class], version = 1, exportSchema = false)
abstract class BodegaDatabase : RoomDatabase() {
    abstract fun bodegaDao(): BodegaDao
}
