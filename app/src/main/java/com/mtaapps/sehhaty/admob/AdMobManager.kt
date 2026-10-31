package com.mtaapps.sehhaty.admob

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.mtaapps.sehhaty.BuildConfig

/**
 * Official AdMob Integration for Sehhaty (MTA TECHNOLOGIES).
 *
 * Production AdMob IDs:
 * - App ID: ca-app-pub-8004171697196640~3905016525
 * - Banner ID: ca-app-pub-8004171697196640/3901147097
 * - Interstitial ID: ca-app-pub-8004171697196640/4886925708
 *
 * Healthcare Ethics & Safety Rules:
 * - NEVER display ads on dose alarm screens, dose confirmation dialogs, or during urgent reminders.
 * - NEVER prevent a patient from confirming taking their medicine.
 * - NEVER send personal health data, medication names, or logs to ad networks.
 * - Use Google official test IDs in debug builds to prevent policy violations.
 */
object AdMobManager {
    // Dynamically uses buildConfig IDs (test IDs in Debug, production IDs in Release)
    val BANNER_AD_UNIT_ID: String
        get() = try {
            BuildConfig.ADMOB_BANNER_ID
        } catch (e: Exception) {
            "ca-app-pub-3940256099942544/6300978111" // Google Official Test Banner
        }

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = try {
            BuildConfig.ADMOB_INTERSTITIAL_ID
        } catch (e: Exception) {
            "ca-app-pub-3940256099942544/1033173712" // Google Official Test Interstitial
        }

    fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
