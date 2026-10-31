package com.mtaapps.sehhaty.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import com.mtaapps.sehhaty.data.remote.FirebaseSyncService
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
            action == Intent.ACTION_TIME_SET ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            val database = SehhatyApp.instance.database
            val scheduler = AlarmScheduler(context)
            val activeUserId = FirebaseSyncService.getActiveUserId(context)

            if (activeUserId == null) {
                Log.i("BootReceiver", "No active user session; skipping alarm restoration.")
                pendingResult.finish()
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val now = System.currentTimeMillis()
                    // Restore pending alarms only for the active account.
                    val upcomingDoses = database.doseDao().getUpcomingDoses(activeUserId, now, DoseStatus.PENDING)
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
