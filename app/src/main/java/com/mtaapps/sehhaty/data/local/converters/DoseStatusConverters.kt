package com.mtaapps.sehhaty.data.local.converters

import androidx.room.TypeConverter
import com.mtaapps.sehhaty.data.local.entity.DoseStatus

/** Persists dose status enum values as stable strings in Room. */
class DoseStatusConverters {
    @TypeConverter
    fun fromDoseStatus(status: DoseStatus): String = status.name

    @TypeConverter
    fun toDoseStatus(value: String): DoseStatus = DoseStatus.valueOf(value)
}
