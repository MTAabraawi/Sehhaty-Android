package com.mtaapps.sehhaty.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Pre-seeded Drug Database for 100% offline search and verified medical reference.
 */
@Entity(tableName = "drug_references")
data class DrugReferenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tradeNameAr: String,
    val tradeNameEn: String,
    val scientificName: String,
    val activeIngredient: String,
    val strength: String,
    val dosageForm: String,
    val approvedIndications: String,
    val contraindications: String,
    val commonSideEffects: String,
    val drugInteractions: String,
    val usageInstructions: String,
    val manufacturer: String,
    val medicalSource: String = "الهيئة العامة للغذاء والدواء / الدليل الصيدلي المعتمد",
    val lastUpdateDate: String = "2025-01-15"
)
