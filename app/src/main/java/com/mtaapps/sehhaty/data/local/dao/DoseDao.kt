package com.mtaapps.sehhaty.data.local.dao

import androidx.room.*
import com.mtaapps.sehhaty.data.local.entity.DoseLogEntity
import com.mtaapps.sehhaty.data.local.entity.DoseScheduleEntity
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseDao {

    @Query("SELECT * FROM dose_schedules WHERE userId = :userId AND scheduledTimeTimestamp BETWEEN :startOfDay AND :endOfDay ORDER BY scheduledTimeTimestamp ASC")
    fun getTodayDoses(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<DoseScheduleEntity>>

    @Query("SELECT * FROM dose_schedules WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getDoseById(id: Long, userId: String): DoseScheduleEntity?

    @Query("SELECT COUNT(*) FROM dose_schedules WHERE medicationId = :medicationId AND userId = :userId AND scheduledTimeTimestamp = :timestamp")
    suspend fun countSchedule(medicationId: Long, userId: String, timestamp: Long): Int

    @Query("SELECT * FROM dose_schedules WHERE userId = :userId AND scheduledTimeTimestamp >= :fromTime AND status = :status ORDER BY scheduledTimeTimestamp ASC")
    suspend fun getUpcomingDoses(userId: String, fromTime: Long, status: DoseStatus = DoseStatus.PENDING): List<DoseScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSchedule(schedule: DoseScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSchedules(schedules: List<DoseScheduleEntity>)

    @Query("UPDATE dose_schedules SET status = :status, supervisorAlerted = :supervisorAlerted WHERE id = :id AND userId = :userId")
    suspend fun updateScheduleState(
        id: Long,
        userId: String,
        status: DoseStatus,
        supervisorAlerted: Boolean
    ): Int

    @Query("UPDATE dose_schedules SET status = :status, confirmedTimeTimestamp = :confirmedTime WHERE id = :id AND userId = :userId")
    suspend fun confirmDose(id: Long, userId: String, status: DoseStatus = DoseStatus.CONFIRMED, confirmedTime: Long = System.currentTimeMillis()): Int

    @Query("UPDATE dose_schedules SET repeatCount = repeatCount + 1 WHERE id = :id AND userId = :userId")
    suspend fun incrementRepeat(id: Long, userId: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLog(log: DoseLogEntity): Long

    @Query("SELECT * FROM dose_logs WHERE userId = :userId ORDER BY confirmedTimeTimestamp DESC LIMIT 100")
    fun getDoseHistory(userId: String): Flow<List<DoseLogEntity>>

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId AND userId = :userId")
    suspend fun deleteSchedulesForMedication(medicationId: Long, userId: String)

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId AND userId = :userId AND status = 'PENDING' AND scheduledTimeTimestamp >= :fromTime")
    suspend fun deleteFuturePendingSchedules(
        medicationId: Long,
        userId: String,
        fromTime: Long = System.currentTimeMillis()
    ): Int

    /**
     * Atomically confirms a dose belonging to the specified user and writes its history log.
     * Prevents duplicate confirmations and cross-account access.
     */
    @Transaction
    suspend fun confirmDoseAndLog(
        doseId: Long,
        userId: String,
        confirmedTime: Long = System.currentTimeMillis()
    ): Boolean {
        val dose = getDoseById(doseId, userId) ?: return false

        if (dose.status == DoseStatus.CONFIRMED) {
            return false
        }

        val updatedRows = confirmDose(doseId, userId, DoseStatus.CONFIRMED, confirmedTime)
        if (updatedRows != 1) return false

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
