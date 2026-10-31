package com.mtaapps.sehhaty.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val scientificName: String = "",
    val activeIngredient: String = "",
    val strength: String, // e.g. "500 mg"
    val dosageForm: String = "قرص", // Tablet, Syrup, Injection, etc.
    val instructions: String = "", // e.g. "بعد الأكل"
    val timesPerDay: Int = 1,
    val doseTimesCsv: String, // Comma-separated times e.g. "08:00,20:00"
    val startDateTimestamp: Long = System.currentTimeMillis(),
    val endDateTimestamp: Long? = null,
    val daysOfWeekMask: Int = 127, // Bitmask for all 7 days by default
    val isPaused: Boolean = false,
    val imagePath: String? = null,
    val userId: String = "local_user",
    val syncedWithFirebase: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
