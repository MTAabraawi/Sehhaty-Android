package com.mtaapps.sehhaty.data.local.dao

import androidx.room.*
import com.mtaapps.sehhaty.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications WHERE userId = :userId ORDER BY name ASC")
    fun getAllMedications(userId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getMedicationById(id: Long, userId: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity): Int

    @Transaction
    suspend fun updateMedicationIfOwned(
        medication: MedicationEntity,
        userId: String
    ): Boolean {
        if (userId.isBlank() || medication.userId != userId) return false
        val existing = getMedicationById(medication.id, userId) ?: return false
        if (existing.userId != medication.userId) return false
        return updateMedication(medication) > 0
    }

    @Query("DELETE FROM medications WHERE id = :id AND userId = :userId")
    suspend fun deleteMedication(id: Long, userId: String): Int

    @Transaction
    suspend fun deleteMedicationIfOwned(id: Long, userId: String): Boolean {
        if (userId.isBlank()) return false
        val existing = getMedicationById(id, userId) ?: return false
        return deleteMedication(existing.id, userId) > 0
    }

    @Query("UPDATE medications SET isPaused = :isPaused WHERE id = :id AND userId = :userId")
    suspend fun setPaused(id: Long, userId: String, isPaused: Boolean): Int
}
