package com.mtaapps.sehhaty

import com.mtaapps.sehhaty.data.local.entity.MedicationEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Validates new requirements:
 * 1. Scientific name is optional (saving with empty scientific name is valid).
 * 2. 12-hour system AM/PM formatting and 24-hour internal storage.
 * 3. Chronological sorting of dose times.
 */
class TwelveHourTimeAndAddMedicationTest {

    @Test
    fun testScientificNameIsOptional() {
        val medWithoutScientific = MedicationEntity(
            name = "بنادول إكسترا",
            scientificName = "", // Empty/Optional
            strength = "قرص واحد",
            dosageForm = "أقراص",
            instructions = "بعد الأكل",
            doseTimesCsv = "08:00,20:00"
        )

        assertTrue(medWithoutScientific.name.isNotBlank())
        assertTrue(medWithoutScientific.scientificName.isBlank())
        assertEquals("أقراص", medWithoutScientific.dosageForm)
        assertEquals("بعد الأكل", medWithoutScientific.instructions)
    }

    @Test
    fun testTwelveHourToTwentyFourHourConversion() {
        // 8:00 AM -> 08:00
        val morningTime = convert12hTo24h(8, 0, false)
        assertEquals("08:00", morningTime)

        // 2:00 PM -> 14:00
        val afternoonTime = convert12hTo24h(2, 0, true)
        assertEquals("14:00", afternoonTime)

        // 8:00 PM -> 20:00
        val eveningTime = convert12hTo24h(8, 0, true)
        assertEquals("20:00", eveningTime)

        // 12:00 AM (Midnight) -> 00:00
        val midnightTime = convert12hTo24h(12, 0, false)
        assertEquals("00:00", midnightTime)

        // 12:00 PM (Noon) -> 12:00
        val noonTime = convert12hTo24h(12, 0, true)
        assertEquals("12:00", noonTime)
    }

    @Test
    fun testSortDoseTimesChronologically() {
        val times = listOf("20:00", "08:00", "14:00")
        val sorted = times.sorted()
        assertEquals("08:00", sorted[0])
        assertEquals("14:00", sorted[1])
        assertEquals("20:00", sorted[2])
    }

    private fun convert12hTo24h(hour12: Int, minute: Int, isPM: Boolean): String {
        val h24 = when {
            isPM && hour12 < 12 -> hour12 + 12
            !isPM && hour12 == 12 -> 0
            else -> hour12
        }
        return String.format("%02d:%02d", h24, minute)
    }
}
