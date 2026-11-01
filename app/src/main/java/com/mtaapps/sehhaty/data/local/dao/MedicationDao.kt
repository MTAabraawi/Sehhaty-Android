package com.mtaapps.sehhaty.data.local.dao

import androidx.room.*
import com.mtaapps.sehhaty.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications WHERE (userId = :userId OR userId = 'local_user') ORDER BY name ASC")
    fun getAllMedications(userId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET isPaused = :isPaused WHERE id = :id")
    suspend fun setPaused(id: Long, isPaused: Boolean)
}
