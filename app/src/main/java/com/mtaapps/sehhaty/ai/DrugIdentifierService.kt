package com.mtaapps.sehhaty.ai

import com.mtaapps.sehhaty.data.local.entity.DrugReferenceEntity

/**
 * Medication Identifier Service for Sehhaty.
 * Matches camera/gallery OCR text or user queries against verified medication formulary.
 *
 * Strict Healthcare Safety & Normalization:
 * - Normalizes Arabic text (alif, hamza, taa marbuta, yaa, and diacritics/tashkeel).
 * - Caps confidence score strictly between 0.0 and 1.0 (100%).
 * - Adheres strictly to medical safety: Never invents medicines, never diagnoses, disclaims info.
 */
object DrugIdentifierService {

    data class RecognitionResult(
        val isRecognized: Boolean,
        val matchedDrug: DrugReferenceEntity?,
        val confidenceScore: Float,
        val extractedKeywords: List<String>,
        val requiresManualConfirmation: Boolean
    )

    /**
     * Normalizes Arabic text for accurate healthcare matching:
     * - Removes Tashkeel (diacritics) and Tatweel.
     * - Normalizes all forms of Alif (أ, إ, آ -> ا).
     * - Normalizes Taa Marbuta (ة -> ه).
     * - Normalizes Yaa (ى -> ي).
     */
    fun normalizeArabic(text: String): String {
        return text
            .replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "") // Remove diacritics & tatweel
            .replace(Regex("[أإآ]"), "ا")
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .trim()
            .lowercase()
    }

    fun analyzeExtractedText(rawOcrText: String, candidateDrugs: List<DrugReferenceEntity>): RecognitionResult {
        if (rawOcrText.isBlank()) {
            return RecognitionResult(
                isRecognized = false,
                matchedDrug = null,
                confidenceScore = 0f,
                extractedKeywords = emptyList(),
                requiresManualConfirmation = true
            )
        }

        val normalizedInput = normalizeArabic(rawOcrText)
        var bestMatch: DrugReferenceEntity? = null
        var highestScore = 0f

        for (drug in candidateDrugs) {
            var score = 0f
            val normAr = normalizeArabic(drug.tradeNameAr)
            val normEn = drug.tradeNameEn.lowercase().trim()
            val normSci = drug.scientificName.lowercase().trim()
            val normActive = normalizeArabic(drug.activeIngredient)
            val normStrength = drug.strength.lowercase().trim()

            // Exact or substring matching
            if (normAr.isNotEmpty() && normalizedInput.contains(normAr)) score += 0.5f
            if (normEn.isNotEmpty() && normalizedInput.contains(normEn)) score += 0.5f
            if (normSci.isNotEmpty() && normalizedInput.contains(normSci)) score += 0.3f
            if (normActive.isNotEmpty() && normalizedInput.contains(normActive)) score += 0.3f
            if (normStrength.isNotEmpty() && normalizedInput.contains(normStrength)) score += 0.2f

            if (score > highestScore) {
                highestScore = score
                bestMatch = drug
            }
        }

        // Cap confidence strictly to 1.0 (100%)
        val finalConfidence = highestScore.coerceIn(0f, 1.0f)
        val isReliable = finalConfidence >= 0.5f

        return RecognitionResult(
            isRecognized = isReliable,
            matchedDrug = bestMatch,
            confidenceScore = finalConfidence,
            extractedKeywords = rawOcrText.split(Regex("[\\s,]+")).filter { it.length > 2 }.take(5),
            requiresManualConfirmation = !isReliable || finalConfidence < 0.8f
        )
    }
}
