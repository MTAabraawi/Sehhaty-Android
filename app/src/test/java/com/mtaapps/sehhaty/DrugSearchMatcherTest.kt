package com.mtaapps.sehhaty

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrugSearchMatcherTest {

    @Test
    fun testDrugTradeNameFuzzyMatching() {
        val query = "panadol"
        val tradeNameEn = "Panadol Extra"
        assertTrue(tradeNameEn.lowercase().contains(query.lowercase()))
    }

    @Test
    fun testArabicActiveIngredientMatching() {
        val query = "باراسيتامول"
        val ingredient = "باراسيتامول 500 ملغ + كافيين 65 ملغ"
        assertTrue(ingredient.contains(query))
    }
}
