package com.mtaapps.sehhaty.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_logs")
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val doseStrength: String,
    val scheduledTimeTimestamp: Long,
    val confirmedTimeTimestamp: Long,
    val status: DoseStatus,
    val userId: String = "local_user",
    val notes: String = ""
)
