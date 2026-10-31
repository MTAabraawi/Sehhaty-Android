package com.mtaapps.sehhaty.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Manages exact and repeating medication alarms.
 * Works 100% offline using Android AlarmManager.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExactDoseAlarm(doseId: Long, triggerAtMillis: Long, medName: String, doseStrength: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_MED_NAME, medName)
            putExtra(EXTRA_DOSE_STRENGTH, doseStrength)
            putExtra(EXTRA_IS_REPEAT, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Android 12+ (API 31) exact alarm check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Schedules the mandatory 2-minute reminder repeat if user hasn't confirmed taking the dose.
     */
    fun scheduleRepeatAlarm(doseId: Long, medName: String, doseStrength: String) {
        val repeatInterval = 2 * 60 * 1000L // Exact 2 minutes
        val triggerAtMillis = System.currentTimeMillis() + repeatInterval

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_MED_NAME, medName)
            putExtra(EXTRA_DOSE_STRENGTH, doseStrength)
            putExtra(EXTRA_IS_REPEAT, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (doseId + 100000).toInt(), // Unique request code for repeat
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelRepeatAlarm(doseId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (doseId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        const val EXTRA_DOSE_ID = "extra_dose_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_DOSE_STRENGTH = "extra_dose_strength"
        const val EXTRA_IS_REPEAT = "extra_is_repeat"
    }
}
