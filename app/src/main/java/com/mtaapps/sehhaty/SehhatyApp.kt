package com.mtaapps.sehhaty

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.mtaapps.sehhaty.data.local.SehhatyDatabase

class SehhatyApp : Application() {

    lateinit var database: SehhatyDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize Room Local Database
        database = SehhatyDatabase.getDatabase(this)

        // 2. Initialize Notification Channels (High Importance for Medication Alarms)
        createNotificationChannels()

        // 3. Initialize Google Mobile Ads SDK (AdMob)
        MobileAds.initialize(this) {}
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // High Priority Dose Reminder Channel
            val doseChannel = NotificationChannel(
                CHANNEL_DOSE_REMINDERS,
                "تذكيرات مواعيد الأدوية | Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات مواعيد الجرعات والتنبيهات المتكررة كل دقيقتين"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            // General & Supervisor Escalation Channel
            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "تنبيهات عامة ومتابعة المشرف",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات متابعة مشرف الأسرة وتحديثات النظام"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(doseChannel)
            manager?.createNotificationChannel(infoChannel)
        }
    }

    companion object {
        const val CHANNEL_DOSE_REMINDERS = "sehhaty_dose_reminders_channel"
        const val CHANNEL_INFO = "sehhaty_info_channel"

        lateinit var instance: SehhatyApp
            private set
    }
}
