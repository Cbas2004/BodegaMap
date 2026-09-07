package com.sebas.bodegamap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface BodegaDao {

    @Query("SELECT * FROM bodegas")
    suspend fun obtenerTodas(): List<BodegaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(bodegas: List<BodegaEntity>)

    @Query("DELETE FROM bodegas")
    suspend fun borrarTodas()

    /** Reemplaza el catálogo completo por la copia más reciente del backend. */
    @Transaction
    suspend fun reemplazarTodas(bodegas: List<BodegaEntity>) {
        borrarTodas()
        insertarTodas(bodegas)
    }
}
