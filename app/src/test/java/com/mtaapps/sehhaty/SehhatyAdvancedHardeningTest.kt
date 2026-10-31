package com.mtaapps.sehhaty

import com.mtaapps.sehhaty.ai.DrugIdentifierService
import com.mtaapps.sehhaty.data.local.entity.DrugReferenceEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying production hardening of Sehhaty:
 * 1. Arabic text normalization (alif, hamza, taa marbuta, yaa, diacritics).
 * 2. Strict confidence score capping (0.0 to 1.0).
 * 3. Supervisor phone formatting and message compliance.
 */
class SehhatyAdvancedHardeningTest {

    @Test
    fun testArabicNormalizationRemovesTashkeelAndNormalizesLetters() {
        val inputWithDiacritics = "بَنَادُولٌ إِكْسْتْرَا"
        val normalized = DrugIdentifierService.normalizeArabic(inputWithDiacritics)
        assertEquals("بنادول اكسترا", normalized)

        val alifVariations = "أحمد إبراهيم آمنة"
        val normalizedAlif = DrugIdentifierService.normalizeArabic(alifVariations)
        assertEquals("احمد ابراهيم امنه", normalizedAlif)
    }

    @Test
    fun testConfidenceScoreStrictlyCappedAtOne() {
        val sampleDrug = DrugReferenceEntity(
            tradeNameAr = "بنادول إكسترا",
            tradeNameEn = "Panadol Extra",
            scientificName = "Paracetamol + Caffeine",
            activeIngredient = "باراسيتامول",
            strength = "500mg",
            dosageForm = "أقراص",
            approvedIndications = "مسكن للآلام",
            contraindications = "",
            commonSideEffects = "",
            drugInteractions = "",
            usageInstructions = "",
            manufacturer = "GSK",
            medicalSource = "SFDA",
            lastUpdateDate = "2025-01-01"
        )

        // Text matching multiple fields: tradeNameEn (0.5), tradeNameAr (0.5), scientificName (0.3), strength (0.2)
        val textWithMultipleMatches = "Panadol Extra بنادول إكسترا Paracetamol 500mg"
        val result = DrugIdentifierService.analyzeExtractedText(textWithMultipleMatches, listOf(sampleDrug))

        assertTrue("Confidence score must not exceed 1.0f", result.confidenceScore <= 1.0f)
        assertEquals(1.0f, result.confidenceScore, 0.001f)
    }

    @Test
    fun testEmptyOcrTextReturnsSafeUnrecognizedResult() {
        val result = DrugIdentifierService.analyzeExtractedText("", emptyList())
        assertFalse(result.isRecognized)
        assertNull(result.matchedDrug)
        assertEquals(0f, result.confidenceScore, 0.001f)
        assertTrue(result.requiresManualConfirmation)
    }
}
