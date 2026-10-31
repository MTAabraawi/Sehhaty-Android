package com.mtaapps.sehhaty.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.remote.FirebaseSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles user actions directly from dose notifications:
 * - ACTION_TAKE_DOSE: Confirms dose, logs history, cancels repeating alarms.
 * - ACTION_SNOOZE: Temporarily snoozes for 10 minutes.
 * Uses goAsync() with try-finally to guarantee safe database updates before receiver finish.
 */
class DoseActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val doseId = intent.getLongExtra(EXTRA_DOSE_ID, -1L)

        // Safety check: ignore invalid IDs before allocating async resources
        if (doseId <= 0L) {
            Log.w("DoseActionReceiver", "Received dose action with invalid doseId: $doseId")
            return
        }

        val pendingResult = goAsync()
        val scheduler = AlarmScheduler(context)
        val database = SehhatyApp.instance.database

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch the actual verified dose record from DB to guarantee correct medicationId
                val activeUserId = FirebaseSyncService.getActiveUserId(context)
                if (activeUserId == null) {
                    Log.w("DoseActionReceiver", "No active session. Ignoring dose action $doseId.")
                    return@launch
                }

                val dose = database.doseDao().getDoseById(doseId, activeUserId)
                if (dose == null) {
                    Log.w("DoseActionReceiver", "Dose $doseId not found in database.")
                    return@launch
                }

                when (action) {
                    ACTION_TAKE_DOSE -> {
                        // 1. Cancel all repeat alarms for this dose
                        scheduler.cancelRepeatAlarm(doseId)

                        // 2. Dismiss the notification
                        NotificationHelper.cancelNotification(context, doseId.toInt())

                        // 3. Atomically confirm dose and insert permanent history log using verified DB data
                        val confirmed = database.doseDao().confirmDoseAndLog(doseId, activeUserId)
                        Log.i("DoseActionReceiver", "Dose $doseId confirmed and logged: $confirmed")
                    }

                    ACTION_SNOOZE -> {
                        // 1. Dismiss notification and cancel current repeating alarm
                        NotificationHelper.cancelNotification(context, doseId.toInt())
                        scheduler.cancelRepeatAlarm(doseId)

                        // 2. Reschedule reminder 10 minutes from now using verified DB names
                        val snoozeTrigger = System.currentTimeMillis() + 10 * 60 * 1000L
                        scheduler.scheduleExactDoseAlarm(
                            doseId = doseId,
                            triggerAtMillis = snoozeTrigger,
                            medName = dose.medicationName,
                            doseStrength = dose.doseStrength
                        )
                        Log.i("DoseActionReceiver", "Dose $doseId snoozed for 10 minutes.")
                    }
                }
            } catch (e: Exception) {
                Log.e("DoseActionReceiver", "Error executing dose action for doseId $doseId", e)
            } finally {
                // Guaranteed completion signal to Android OS
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TAKE_DOSE = "com.mtaapps.sehhaty.ACTION_TAKE_DOSE"
        const val ACTION_SNOOZE = "com.mtaapps.sehhaty.ACTION_SNOOZE"
        const val EXTRA_DOSE_ID = "extra_dose_id"
        const val EXTRA_MED_ID = "extra_med_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_DOSE_STRENGTH = "extra_dose_strength"
    }
}
