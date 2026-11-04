package com.mtaapps.sehhaty.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/** Schedules medication alarms, falling back to inexact alarms when exact access is unavailable. */
class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExactDoseAlarm(doseId: Long, triggerAtMillis: Long, medName: String, doseStrength: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_MED_NAME, medName)
            putExtra(EXTRA_DOSE_STRENGTH, doseStrength)
            putExtra(EXTRA_IS_REPEAT, false)
        }
        val pi = PendingIntent.getBroadcast(context, doseId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        schedule(triggerAtMillis, pi)
    }

    fun scheduleRepeatAlarm(doseId: Long, medName: String, doseStrength: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_MED_NAME, medName)
            putExtra(EXTRA_DOSE_STRENGTH, doseStrength)
            putExtra(EXTRA_IS_REPEAT, true)
        }
        val pi = PendingIntent.getBroadcast(context, repeatRequestCode(doseId), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        schedule(System.currentTimeMillis() + REPEAT_INTERVAL_MILLIS, pi)
    }

    private fun schedule(triggerAt: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancelDoseAlarm(doseId: Long) = cancel(doseId.toInt())
    fun cancelRepeatAlarm(doseId: Long) = cancel(repeatRequestCode(doseId))

    fun cancelAllDoseAlarms(doseId: Long) {
        cancelDoseAlarm(doseId)
        cancelRepeatAlarm(doseId)
    }

    private fun cancel(code: Int) {
        val pi = PendingIntent.getBroadcast(context, code, Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) ?: return
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun repeatRequestCode(id: Long) = (id + REPEAT_REQUEST_CODE_OFFSET).toInt()

    companion object {
        const val EXTRA_DOSE_ID = "extra_dose_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_DOSE_STRENGTH = "extra_dose_strength"
        const val EXTRA_IS_REPEAT = "extra_is_repeat"
        private const val REPEAT_INTERVAL_MILLIS = 2 * 60 * 1000L
        private const val REPEAT_REQUEST_CODE_OFFSET = 100000L
    }
}
