package com.mtaapps.sehhaty.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.mtaapps.sehhaty.MainActivity
import com.mtaapps.sehhaty.R
import com.mtaapps.sehhaty.SehhatyApp

object NotificationHelper {

    fun showDoseNotification(
        context: Context,
        doseId: Long,
        medicationName: String,
        doseStrength: String,
        isRepeat: Boolean
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            doseId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: "تم تناول الجرعة" (Confirm Taken)
        val takeIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = DoseActionReceiver.ACTION_TAKE_DOSE
            putExtra(DoseActionReceiver.EXTRA_DOSE_ID, doseId)
            putExtra(DoseActionReceiver.EXTRA_MED_NAME, medicationName)
            putExtra(DoseActionReceiver.EXTRA_DOSE_STRENGTH, doseStrength)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            (doseId * 10 + 1).toInt(),
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: "تأجيل 10 دقائق" (Snooze)
        val snoozeIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = DoseActionReceiver.ACTION_SNOOZE
            putExtra(DoseActionReceiver.EXTRA_DOSE_ID, doseId)
            putExtra(DoseActionReceiver.EXTRA_MED_NAME, medicationName)
            putExtra(DoseActionReceiver.EXTRA_DOSE_STRENGTH, doseStrength)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (doseId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = if (isRepeat) {
            "تذكير متكرر: حان موعد دواء $medicationName"
        } else {
            "موعد جرعة دواء: $medicationName"
        }

        val body = "الجرعة المقررة: $doseStrength\nيرجى تأكيد تناول الدواء لإيقاف التذكيرات."

        val notification = NotificationCompat.Builder(context, SehhatyApp.CHANNEL_DOSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true) // Persistent until user acts
            .addAction(R.drawable.ic_launcher_foreground, "تم تناول الجرعة ✓", takePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "تأجيل 10 دقائق ⏰", snoozePendingIntent)
            .build()

        manager.notify(doseId.toInt(), notification)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}
