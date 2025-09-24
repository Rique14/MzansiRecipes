package com.mzansi.recipes.data.repo

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // For merging data
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val fs: FirebaseFirestore
) {
    suspend fun register(name: String, email: String, password: String) {
        val res = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = res.user?.uid ?: return
        val userData = mapOf(
            "name" to name,
            "email" to email,
            "preferredLanguage" to "en",
            "theme" to "system",
            "notificationsEnabled" to true
        )
        fs.collection("users").document(uid).set(userData).await()
        Log.d("AuthRepository", "User registered and Firestore document created for UID: $uid")
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
        Log.d("AuthRepository", "User logged in: $email")
    }

    suspend fun forgot(email: String) {
        auth.sendPasswordResetEmail(email).await()
        Log.d("AuthRepository", "Password reset email sent to: $email")
    }

    suspend fun signInWithCredentialAndManageUser(credential: AuthCredential) {
        val authResult = auth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            Log.d("AuthRepository", "Signed in with credential for user: ${firebaseUser.uid}, Email: ${firebaseUser.email}")
            // Check if user is new or if we need to update their Firestore document
            val isNewUser = authResult.additionalUserInfo?.isNewUser == true
            Log.d("AuthRepository", "Is new user: $isNewUser")

            // Always try to set/update Firestore for Google users to ensure profile info is fresh
            // and default fields are set if it's their first time.
            val name = firebaseUser.displayName ?: "User"
            val email = firebaseUser.email ?: ""

            val userData = mutableMapOf<String, Any>(
                "name" to name,
                "email" to email
            )
            
            // For new users, also set default preferences.
            // For existing users, this will only update name and email if they changed in Google account,
            // thanks to SetOptions.merge().
            if (isNewUser) {
                userData["preferredLanguage"] = "en"
                userData["theme"] = "system"
                userData["notificationsEnabled"] = true
                Log.d("AuthRepository", "Setting default preferences for new Google user: ${firebaseUser.uid}")
                 fs.collection("users").document(firebaseUser.uid).set(userData).await()
            } else {
                 // For existing user, merge to update only name/email if changed, and preserve other settings
                Log.d("AuthRepository", "Updating existing user data for Google user: ${firebaseUser.uid}")
                fs.collection("users").document(firebaseUser.uid).set(userData, SetOptions.merge()).await()
            }
            Log.d("AuthRepository", "Firestore document managed for user: ${firebaseUser.uid}")
        } else {
            Log.e("AuthRepository", "Firebase user is null after signing in with credential.")
            throw IllegalStateException("Firebase user is null after successful credential sign-in.")
        }
    }

    fun currentUserId(): String? = auth.currentUser?.uid
    fun logout() {
        auth.signOut()
        Log.d("AuthRepository", "User logged out.")
        // Consider also signing out from GoogleSignInClient if it was used
        // However, this is usually handled at the UI layer where GoogleSignInClient is managed.
    }
}
