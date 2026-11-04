package com.mtaapps.sehhaty.data.repository

import android.content.Context
import com.mtaapps.sehhaty.alarm.AlarmScheduler
import com.mtaapps.sehhaty.data.local.dao.DoseDao
import com.mtaapps.sehhaty.data.local.dao.MedicationDao
import com.mtaapps.sehhaty.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.*

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val doseDao: DoseDao
) {
    fun getMedications(userId: String): Flow<List<MedicationEntity>> =
        medicationDao.getAllMedications(userId)

    suspend fun getMedicationById(id: Long): MedicationEntity? =
        medicationDao.getMedicationById(id)

    suspend fun addMedication(medication: MedicationEntity): Long {
        val medId = medicationDao.insertMedication(medication)
        generateDoseSchedulesForMedication(medication.copy(id = medId))
        return medId
    }

    suspend fun updateMedication(medication: MedicationEntity) {
        medicationDao.updateMedication(medication)
        // Refresh future pending schedules without wiping past confirmed logs
        doseDao.deleteSchedulesForMedication(medication.id)
        generateDoseSchedulesForMedication(medication)
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        // Cascade delete future schedules; history logs are safely retained in dose_logs table
        doseDao.deleteSchedulesForMedication(medication.id)
        medicationDao.deleteMedication(medication)
    }

    suspend fun scheduleAlarmsForMedication(context: Context, medicationId: Long) {
        val scheduler = AlarmScheduler(context)
        val upcoming = doseDao.getUpcomingDosesForMedication(medicationId)
        for (dose in upcoming) {
            scheduler.scheduleExactDoseAlarm(
                doseId = dose.id,
                triggerAtMillis = dose.scheduledTimeTimestamp,
                medName = dose.medicationName,
                doseStrength = dose.doseStrength
            )
        }
    }

    suspend fun setPaused(medicationId: Long, isPaused: Boolean) {
        medicationDao.setPaused(medicationId, isPaused)
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

    suspend fun confirmDose(doseId: Long, medicationId: Long, medName: String, doseStrength: String, scheduledTime: Long) {
        // Atomically confirm and log through the transactional DAO method
        doseDao.confirmDoseAndLog(doseId)
    }

    fun getDoseHistory(userId: String): Flow<List<DoseLogEntity>> =
        doseDao.getDoseHistory(userId)

    private suspend fun generateDoseSchedulesForMedication(medication: MedicationEntity) {
        if (medication.isPaused) return

        val times = medication.doseTimesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val schedules = mutableListOf<DoseScheduleEntity>()
        val today = Calendar.getInstance()

        // Generate schedules for next 7 days
        for (dayOffset in 0..7) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            val dayIndex = dayCal.get(Calendar.DAY_OF_WEEK) - 1
            val dayBit = 1 shl dayIndex

            // Check days of week mask
            if ((medication.daysOfWeekMask and dayBit) == 0) {
                continue
            }

            for (timeStr in times) {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: continue
                    val minute = parts[1].toIntOrNull() ?: continue
                    if (hour !in 0..23 || minute !in 0..59) continue
                    val doseCal = Calendar.getInstance().apply {
                        timeInMillis = dayCal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val scheduledMillis = doseCal.timeInMillis

                    // Respect start date
                    if (scheduledMillis < medication.startDateTimestamp) {
                        continue
                    }

                    // Respect end date
                    if (medication.endDateTimestamp != null && scheduledMillis > medication.endDateTimestamp) {
                        continue
                    }

                    // Prevent duplicates
                    if (doseDao.countSchedule(medication.id, scheduledMillis) > 0) {
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
        }

        if (schedules.isNotEmpty()) {
            doseDao.insertSchedules(schedules)
        }
    }
}
