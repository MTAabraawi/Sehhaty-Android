package com.mtaapps.sehhaty.supervisor

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import java.net.URLEncoder

/**
 * Handles family supervisor notification and WhatsApp alert escalation.
 * Uses official WhatsApp URI intent with direct package targeting ("com.whatsapp").
 *
 * Android & WhatsApp Security/Privacy Note:
 * WhatsApp does NOT provide an official background API to send messages silently from
 * a personal WhatsApp account without user initiation. The closest official, safe, and legal
 * flow is pre-filling the chat with the verified phone number and message so the user only
 * taps the send arrow (1-tap).
 */
object SupervisorAlertManager {

    suspend fun checkAndEscalateIfNeeded(
        context: Context,
        doseId: Long,
        medicationName: String,
        doseStrength: String
    ) {
        val database = SehhatyApp.instance.database
        val supervisor = database.supervisorDao().getSupervisorSync("local_user") ?: return
        val dose = database.doseDao().getDoseById(doseId) ?: return

        if (!supervisor.isActive || !supervisor.consentGiven) return
        if (dose.status == DoseStatus.CONFIRMED || dose.supervisorAlerted) return

        val delayMillis = supervisor.alertDelayMinutes * 60 * 1000L
        val elapsedSinceSchedule = System.currentTimeMillis() - dose.scheduledTimeTimestamp

        if (elapsedSinceSchedule >= delayMillis) {
            database.doseDao().updateSchedule(dose.copy(supervisorAlerted = true, status = DoseStatus.UNCONFIRMED))
        }
    }

    /**
     * Builds official respectful WhatsApp intent message targeted directly to WhatsApp client.
     */
    fun createWhatsAppIntent(
        supervisorPhoneWithCountryCode: String,
        medicationName: String,
        doseStrength: String
    ): Intent {
        val cleanNumber = supervisorPhoneWithCountryCode.replace("+", "").replace(" ", "").trim()
        val message = "تنبيه من تطبيق صحتي: لم يتم تأكيد تناول الجرعة المسجلة لدواء ($medicationName - $doseStrength) في موعدها. يرجى التواصل مع صاحب الحساب للاطمئنان عليه."
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMessage")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Direct package targeting for immediate opening in WhatsApp
            setPackage("com.whatsapp")
        }
        return intent
    }
}
