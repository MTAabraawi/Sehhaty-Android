package com.mtaapps.sehhaty.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_supervisors")
data class SupervisorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String, // الأب، الأم، الأخ، الأخت، الابن، الابنة، الزوج/الزوجة، آخر
    val phoneNumber: String,
    val whatsappNumber: String, // With country code e.g. +966500000000
    val consentGiven: Boolean = true,
    val alertDelayMinutes: Int = 30, // Delay before sending alert
    val isActive: Boolean = true,
    val userId: String = "local_user"
)
