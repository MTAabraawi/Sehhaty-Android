package com.mtaapps.sehhaty.supervisor

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.remote.FirebaseSyncService
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import java.net.URLEncoder

/**
 * Handles family supervisor notification and WhatsApp alert escalation.
 * Respects medical ethics: Phrases alert as "لم يتم تأكيد الجرعة" (Dose not confirmed)
 * rather than assuming the patient did not take it.
 */
object SupervisorAlertManager {

    suspend fun checkAndEscalateIfNeeded(
        context: Context,
        doseId: Long,
        medicationName: String,
        doseStrength: String
    ) {
        val database = SehhatyApp.instance.database
        val activeUserId = FirebaseSyncService.getActiveUserId(context) ?: return
        val dose = database.doseDao().getDoseById(doseId, activeUserId) ?: return
        val supervisor = database.supervisorDao().getSupervisorSync(dose.userId) ?: return

        if (!supervisor.isActive || !supervisor.consentGiven) return
        if (dose.status == DoseStatus.CONFIRMED || dose.supervisorAlerted) return

        val delayMillis = supervisor.alertDelayMinutes * 60 * 1000L
        val elapsedSinceSchedule = System.currentTimeMillis() - dose.scheduledTimeTimestamp

        if (elapsedSinceSchedule >= delayMillis) {
            // Threshold reached! Mark as alerted to prevent spam
            database.doseDao().updateScheduleState(
                id = dose.id,
                userId = dose.userId,
                status = DoseStatus.UNCONFIRMED,
                supervisorAlerted = true
            )
        }
    }

    /**
     * Builds official respectful WhatsApp intent message.
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
        return Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
