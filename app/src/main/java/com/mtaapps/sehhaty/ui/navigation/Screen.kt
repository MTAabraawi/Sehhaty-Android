package com.mtaapps.sehhaty.ui.navigation

sealed class Screen(val route: String, val titleAr: String, val titleEn: String) {
    object Splash : Screen("splash", "صحتي", "Sehhaty")
    object Auth : Screen("auth", "تسجيل الدخول", "Sign In")
    object Home : Screen("home", "الرئيسية", "Home")
    object Medications : Screen("medications", "أدويتي", "Medications")
    object DrugSearch : Screen("drug_search", "البحث والتعرف الذكي", "Drug Search")
    object AddMedication : Screen("add_medication", "إضافة دواء جديد", "Add Medication")
    object DoseHistory : Screen("history", "سجل الجرعات", "History")
    object Supervisor : Screen("supervisor", "مشرف الأسرة", "Family Care")
    object Settings : Screen("settings", "الإعدادات", "Settings")
}
