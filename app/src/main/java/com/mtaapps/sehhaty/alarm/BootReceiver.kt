package com.mtaapps.sehhaty.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Automatically restores all pending medication alarms on device boot,
 * timezone change, time adjustment, or app update.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            val database = SehhatyApp.instance.database
            val scheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val now = System.currentTimeMillis()
                    // Fetch upcoming pending doses from Room database
                    val upcomingDoses = database.doseDao().getUpcomingDoses(now, DoseStatus.PENDING)
                    for (dose in upcomingDoses) {
                        scheduler.scheduleExactDoseAlarm(
                            doseId = dose.id,
                            triggerAtMillis = dose.scheduledTimeTimestamp,
                            medName = dose.medicationName,
                            doseStrength = dose.doseStrength
                        )
                    }
                    Log.i("BootReceiver", "Successfully restored ${upcomingDoses.size} medication alarms.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error restoring alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
