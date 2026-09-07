package com.sebas.bodegamap

import android.app.Application
import androidx.room.Room
import com.sebas.bodegamap.data.local.BodegaDatabase

class BodegaMapApplication : Application() {

    val database: BodegaDatabase by lazy {
        Room.databaseBuilder(this, BodegaDatabase::class.java, "bodegamap.db").build()
    }
}
