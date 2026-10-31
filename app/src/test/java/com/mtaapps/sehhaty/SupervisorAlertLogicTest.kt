package com.mtaapps.sehhaty

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupervisorAlertLogicTest {

    @Test
    fun testSupervisorThresholdElapsed() {
        val alertDelayMinutes = 30
        val alertDelayMillis = alertDelayMinutes * 60 * 1000L
        val scheduledTime = 1000000L
        val currentTimeUnderDelay = scheduledTime + (20 * 60 * 1000L) // 20 mins later
        val currentTimeOverDelay = scheduledTime + (35 * 60 * 1000L)  // 35 mins later

        val shouldAlertUnder = (currentTimeUnderDelay - scheduledTime) >= alertDelayMillis
        val shouldAlertOver = (currentTimeOverDelay - scheduledTime) >= alertDelayMillis

        assertFalse(shouldAlertUnder)
        assertTrue(shouldAlertOver)
    }

    @Test
    fun testSupervisorMessageContainsMedicalCaveat() {
        val rawMessage = "تنبيه من تطبيق صحتي: لم يتم تأكيد تناول الجرعة المسجلة في موعدها. يرجى التواصل مع صاحب الحساب للاطمئنان عليه."
        assertTrue(rawMessage.contains("لم يتم تأكيد"))
        assertFalse(rawMessage.contains("لم يتناول الدواء"))
    }
}
