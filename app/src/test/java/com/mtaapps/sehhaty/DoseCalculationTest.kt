package com.mtaapps.sehhaty

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseCalculationTest {

    @Test
    fun testParseDoseTimes() {
        val doseCsv = "08:00, 14:00, 20:00"
        val times = doseCsv.split(",").map { it.trim() }
        assertEquals(3, times.size)
        assertEquals("08:00", times[0])
        assertEquals("14:00", times[1])
        assertEquals("20:00", times[2])
    }

    @Test
    fun testTwoMinuteRepeatIntervalCalculation() {
        val repeatIntervalMinutes = 2
        val repeatIntervalMillis = repeatIntervalMinutes * 60 * 1000L
        assertEquals(120000L, repeatIntervalMillis)
    }

    @Test
    fun testDaysOfWeekBitmask() {
        val allDaysMask = 127 // Binary 1111111 (all 7 days)
        assertTrue((allDaysMask and (1 shl 0)) != 0) // Sunday
        assertTrue((allDaysMask and (1 shl 6)) != 0) // Saturday
    }
}
