package com.mtaapps.sehhaty.data.local.dao

import androidx.room.*
import com.mtaapps.sehhaty.data.local.entity.DoseLogEntity
import com.mtaapps.sehhaty.data.local.entity.DoseScheduleEntity
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseDao {

    @Query("SELECT * FROM dose_schedules WHERE (userId = :userId OR userId = 'local_user') AND scheduledTimeTimestamp BETWEEN :startOfDay AND :endOfDay ORDER BY scheduledTimeTimestamp ASC")
    fun getTodayDoses(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<DoseScheduleEntity>>

    @Query("SELECT * FROM dose_schedules WHERE id = :id LIMIT 1")
    suspend fun getDoseById(id: Long): DoseScheduleEntity?

    @Query("SELECT COUNT(*) FROM dose_schedules WHERE medicationId = :medicationId AND scheduledTimeTimestamp = :timestamp")
    suspend fun countSchedule(medicationId: Long, timestamp: Long): Int

    @Query("SELECT * FROM dose_schedules WHERE scheduledTimeTimestamp >= :fromTime AND status = :status ORDER BY scheduledTimeTimestamp ASC")
    suspend fun getUpcomingDoses(fromTime: Long, status: DoseStatus = DoseStatus.PENDING): List<DoseScheduleEntity>

    @Query("SELECT * FROM dose_schedules WHERE medicationId = :medicationId AND scheduledTimeTimestamp >= :fromTime AND status = :status ORDER BY scheduledTimeTimestamp ASC")
    suspend fun getUpcomingDosesForMedication(medicationId: Long, fromTime: Long = System.currentTimeMillis(), status: DoseStatus = DoseStatus.PENDING): List<DoseScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DoseScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSchedules(schedules: List<DoseScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: DoseScheduleEntity)

    @Query("UPDATE dose_schedules SET status = :status, confirmedTimeTimestamp = :confirmedTime WHERE id = :id")
    suspend fun confirmDose(id: Long, status: DoseStatus = DoseStatus.CONFIRMED, confirmedTime: Long = System.currentTimeMillis())

    @Query("UPDATE dose_schedules SET repeatCount = repeatCount + 1 WHERE id = :id")
    suspend fun incrementRepeat(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DoseLogEntity): Long

    @Query("SELECT * FROM dose_logs WHERE (userId = :userId OR userId = 'local_user') ORDER BY confirmedTimeTimestamp DESC LIMIT 100")
    fun getDoseHistory(userId: String): Flow<List<DoseLogEntity>>

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)

    /**
     * Atomic transaction to confirm dose and write to permanent history log.
     * Prevents duplicate confirmations and guarantees data integrity.
     */
    @Transaction
    suspend fun confirmDoseAndLog(doseId: Long, confirmedTime: Long = System.currentTimeMillis()): Boolean {
        val dose = getDoseById(doseId) ?: return false
        // Prevent duplicate confirmations
        if (dose.status == DoseStatus.CONFIRMED) {
            return false
        }
        confirmDose(doseId, DoseStatus.CONFIRMED, confirmedTime)
        val log = DoseLogEntity(
            medicationId = dose.medicationId,
            medicationName = dose.medicationName,
            doseStrength = dose.doseStrength,
            scheduledTimeTimestamp = dose.scheduledTimeTimestamp,
            confirmedTimeTimestamp = confirmedTime,
            status = DoseStatus.CONFIRMED,
            userId = dose.userId
        )
        insertLog(log)
        return true
    }
}
