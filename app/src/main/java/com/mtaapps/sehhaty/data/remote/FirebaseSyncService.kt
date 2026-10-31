package com.mtaapps.sehhaty.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Safe Firebase synchronization service.
 * Operates gracefully whether Firebase is configured with real credentials or offline.
 */
object FirebaseSyncService {

    val isFirebaseConfigured: Boolean
        get() = try {
            FirebaseApp.getInstance() != null
        } catch (e: Exception) {
            false
        }

    val currentUserId: String?
        get() = if (isFirebaseConfigured) FirebaseAuth.getInstance().currentUser?.uid else null

    /**
     * Returns the active authenticated Firebase user or explicitly enabled local session.
     * Returns null when no valid session exists.
     */
    fun getActiveUserId(context: Context): String? {
        val firebaseUserId = currentUserId
        if (!firebaseUserId.isNullOrBlank()) return firebaseUserId

        val preferences = context.getSharedPreferences("sehhaty_session", Context.MODE_PRIVATE)
        return if (preferences.getBoolean("local_mode_enabled", false)) {
            "local_user"
        } else {
            null
        }
    }

    suspend fun syncUserData(context: Context, userId: String): Result<Boolean> {
        if (!isFirebaseConfigured || userId == "local_user") {
            return Result.success(false) // Offline mode; local Room database used
        }

        return try {
            val firestore = FirebaseFirestore.getInstance()
            // Strictly scoped to the authenticated user's private document tree:
            val userRef = firestore.collection("users").document(userId)
            val snapshot = userRef.get().await()
            Result.success(snapshot.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
