package com.mtaapps.sehhaty.data.repository

import androidx.room.withTransaction

import com.mtaapps.sehhaty.data.local.dao.DoseDao
import com.mtaapps.sehhaty.data.local.dao.MedicationDao
import com.mtaapps.sehhaty.data.local.SehhatyDatabase
import com.mtaapps.sehhaty.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.*

class MedicationRepository(
    private val database: SehhatyDatabase,
    private val medicationDao: MedicationDao,
    private val doseDao: DoseDao
) {
    fun getMedications(userId: String): Flow<List<MedicationEntity>> =
        medicationDao.getAllMedications(userId)

    suspend fun getMedicationById(id: Long, userId: String): MedicationEntity? =
        medicationDao.getMedicationById(id, userId)

    suspend fun addMedication(
        medication: MedicationEntity,
        activeUserId: String
    ): Long {
        require(activeUserId.isNotBlank()) { "No active user session" }
        require(medication.userId == activeUserId) {
            "Medication does not belong to the active user"
        }

        return database.withTransaction {
            val medId = medicationDao.insertMedication(medication)
            generateDoseSchedulesForMedication(medication.copy(id = medId))
            medId
        }
    }

    suspend fun updateMedication(
        medication: MedicationEntity,
        activeUserId: String
    ): Boolean {
        require(activeUserId.isNotBlank()) { "No active user session" }
        require(medication.userId == activeUserId) {
            "Medication does not belong to the active user"
        }

        return database.withTransaction {
            val updated = medicationDao.updateMedicationIfOwned(medication, activeUserId)
            if (!updated) return@withTransaction false

            // Refresh future pending schedules without wiping past confirmed logs.
            doseDao.deleteFuturePendingSchedules(medication.id, activeUserId)
            generateDoseSchedulesForMedication(medication)
            true
        }
    }

    suspend fun deleteMedication(
        medication: MedicationEntity,
        activeUserId: String
    ): Boolean {
        require(activeUserId.isNotBlank()) { "No active user session" }
        require(medication.userId == activeUserId) {
            "Medication does not belong to the active user"
        }
        return medicationDao.deleteMedicationIfOwned(medication.id, activeUserId)
    }

    suspend fun setPaused(medicationId: Long, userId: String, isPaused: Boolean): Boolean {
        require(userId.isNotBlank()) { "No active user session" }

        return database.withTransaction {
            val medication = medicationDao.getMedicationById(medicationId, userId)
                ?: return@withTransaction false

            if (medication.isPaused == isPaused) {
                return@withTransaction true
            }

            val updatedRows = medicationDao.setPaused(medicationId, userId, isPaused)
            if (updatedRows != 1) return@withTransaction false

            if (isPaused) {
                doseDao.deleteFuturePendingSchedules(medicationId, userId)
            } else {
                generateDoseSchedulesForMedication(medication.copy(isPaused = false))
            }

            true
        }
    }

    fun getTodayDoses(userId: String): Flow<List<DoseScheduleEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis
        return doseDao.getTodayDoses(userId, startOfDay, endOfDay)
    }

    suspend fun confirmDose(doseId: Long, userId: String, medicationId: Long, medName: String, doseStrength: String, scheduledTime: Long) {
        // Atomically confirm and log through the transactional DAO method
        doseDao.confirmDoseAndLog(doseId, userId)
    }

    fun getDoseHistory(userId: String): Flow<List<DoseLogEntity>> =
        doseDao.getDoseHistory(userId)

    private fun parseDoseTimes(value: String): List<Pair<Int, Int>> {
        val entries = value.split(",").map { it.trim() }

        require(entries.isNotEmpty() && entries.all { it.isNotEmpty() }) {
            "يجب إدخال موعد واحد على الأقل دون ترك مواعيد فارغة"
        }

        val parsed = entries.map { entry ->
            require(Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$").matches(entry)) {
                "صيغة الموعد غير صحيحة: $entry. استخدم HH:mm مثل 08:00"
            }

            val parts = entry.split(":")
            parts[0].toInt() to parts[1].toInt()
        }

        require(parsed.distinct().size == parsed.size) {
            "لا يمكن تكرار موعد الجرعة نفسه"
        }

        return parsed
    }

    /**
     * Generates dose schedules strictly respecting:
     * - isPaused: If paused, do not schedule reminders.
     * - startDateTimestamp: Do not schedule before treatment starts.
     * - endDateTimestamp: Do not schedule after treatment finishes.
     * - daysOfWeekMask: Only schedule on patient-selected days of the week.
     * - Uniqueness: Avoid duplicate schedules for the same medication at the same time.
     */
    private suspend fun generateDoseSchedulesForMedication(medication: MedicationEntity) {
        val times = parseDoseTimes(medication.doseTimesCsv)
        if (medication.isPaused) return
        val schedules = mutableListOf<DoseScheduleEntity>()
        val today = Calendar.getInstance()

        // Generate schedules for next 7 days
        for (dayOffset in 0..7) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ... Saturday=7
            // Convert to 0-indexed: Sunday=0, Monday=1, ... Saturday=6
            val dayIndex = dayCal.get(Calendar.DAY_OF_WEEK) - 1
            val dayBit = 1 shl dayIndex

            // Check days of week mask
            if ((medication.daysOfWeekMask and dayBit) == 0) {
                continue
            }

            for ((hour, minute) in times) {
                val doseCal = Calendar.getInstance().apply {
                    timeInMillis = dayCal.timeInMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val scheduledMillis = doseCal.timeInMillis

                // Skip dose times that have already passed.
                if (scheduledMillis <= System.currentTimeMillis()) {
                    continue
                }

                // Respect start date
                if (scheduledMillis < medication.startDateTimestamp) {
                    continue
                }

                // Respect end date
                if (medication.endDateTimestamp != null && scheduledMillis > medication.endDateTimestamp) {
                    continue
                }

                // Prevent duplicates
                if (doseDao.countSchedule(medication.id, medication.userId, scheduledMillis) > 0) {
                    continue
                }

                schedules.add(
                    DoseScheduleEntity(
                        medicationId = medication.id,
                        medicationName = medication.name,
                        doseStrength = medication.strength,
                        scheduledTimeTimestamp = scheduledMillis,
                        status = DoseStatus.PENDING,
                        userId = medication.userId
                    )
                )
            }
        }

        if (schedules.isNotEmpty()) {
            doseDao.insertSchedules(schedules)
        }
    }
}
