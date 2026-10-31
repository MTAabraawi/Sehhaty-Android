package com.mtaapps.sehhaty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Status of the scheduled dose:
 * PENDING = بانتظار الموعد
 * DUE = حان موعد الجرعة (Repeats reminder every 2 minutes)
 * CONFIRMED = تم تأكيد تناول الجرعة (Stops repeating)
 * UNCONFIRMED = لم يتم تأكيد الجرعة (Escalates to supervisor after delay)
 */
enum class DoseStatus {
    PENDING,
    DUE,
    CONFIRMED,
    UNCONFIRMED
}

@Entity(
    tableName = "dose_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId"), Index("scheduledTimeTimestamp")]
)
data class DoseScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val doseStrength: String,
    val scheduledTimeTimestamp: Long, // Exact epoch millis
    val status: DoseStatus = DoseStatus.PENDING,
    val confirmedTimeTimestamp: Long? = null,
    val repeatCount: Int = 0,
    val supervisorAlerted: Boolean = false,
    val userId: String = "local_user"
)
