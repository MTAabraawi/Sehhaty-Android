package com.mtaapps.sehhaty.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import com.mtaapps.sehhaty.data.remote.FirebaseSyncService
import com.mtaapps.sehhaty.supervisor.SupervisorAlertManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles exact medication alarm broadcasts and 2-minute reminder repeats.
 * Uses goAsync() with try-finally to prevent process termination.
 * Enforces rational repeat limits (max 15 repeats = 30 minutes) to protect battery.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getLongExtra(AlarmScheduler.EXTRA_DOSE_ID, -1L)
        val isRepeat = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_REPEAT, false)

        if (doseId <= 0L) {
            Log.w("AlarmReceiver", "Alarm received with invalid doseId: $doseId")
            return
        }

        val pendingResult = goAsync()
        val database = SehhatyApp.instance.database
        val alarmScheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeUserId = FirebaseSyncService.getActiveUserId(context)
                if (activeUserId == null) {
                    Log.w("AlarmReceiver", "No active session. Ignoring dose alarm $doseId.")
                    alarmScheduler.cancelRepeatAlarm(doseId)
                    NotificationHelper.cancelNotification(context, doseId.toInt())
                    return@launch
                }

                val dose = database.doseDao().getDoseById(doseId, activeUserId)
                if (dose == null) {
                    Log.w("AlarmReceiver", "Dose $doseId not found in database. Cancelling alarm.")
                    alarmScheduler.cancelRepeatAlarm(doseId)
                    NotificationHelper.cancelNotification(context, doseId.toInt())
                    return@launch
                }

                // If user already confirmed taking the dose, stop repeating immediately!
                if (dose.status == DoseStatus.CONFIRMED) {
                    Log.i("AlarmReceiver", "Dose $doseId already confirmed. Cancelling repeats.")
                    alarmScheduler.cancelRepeatAlarm(doseId)
                    NotificationHelper.cancelNotification(context, doseId.toInt())
                    return@launch
                }

                val currentRepeats = dose.repeatCount

                // Maximum repeat threshold: 15 repeats of 2 minutes = 30 minutes total
                if (currentRepeats >= MAX_REPEAT_COUNT) {
                    Log.i("AlarmReceiver", "Max reminder repeats ($MAX_REPEAT_COUNT) reached for dose $doseId.")
                    alarmScheduler.cancelRepeatAlarm(doseId)
                    database.doseDao().updateScheduleState(
                        id = dose.id,
                        userId = dose.userId,
                        status = DoseStatus.UNCONFIRMED,
                        supervisorAlerted = dose.supervisorAlerted
                    )
                    // Escalate to family supervisor if configured
                    SupervisorAlertManager.checkAndEscalateIfNeeded(
                        context,
                        doseId,
                        dose.medicationName,
                        dose.doseStrength
                    )
                    return@launch
                }

                // Increment repeat count
                database.doseDao().incrementRepeat(doseId, activeUserId)

                // Trigger High-Priority Alarm Notification with Take / Snooze Action Buttons
                NotificationHelper.showDoseNotification(
                    context = context,
                    doseId = doseId,
                    medicationName = dose.medicationName,
                    doseStrength = dose.doseStrength,
                    isRepeat = isRepeat
                )

                // Schedule the next 2-minute reminder repeat
                alarmScheduler.scheduleRepeatAlarm(doseId, dose.medicationName, dose.doseStrength)

                // Check supervisor escalation threshold (e.g. 30 minutes)
                SupervisorAlertManager.checkAndEscalateIfNeeded(
                    context,
                    doseId,
                    dose.medicationName,
                    dose.doseStrength
                )

            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm broadcast for dose $doseId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val MAX_REPEAT_COUNT = 15 // 15 repeats * 2 minutes = 30 minutes
    }
}
