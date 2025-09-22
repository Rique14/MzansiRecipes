package com.mzansi.recipes.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val fs: FirebaseFirestore
) {
    suspend fun register(name: String, email: String, password: String) {
        val res = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = res.user?.uid ?: return
        fs.collection("users").document(uid).set(
            mapOf(
                "name" to name,
                "email" to email,
                "preferredLanguage" to "en",
                "theme" to "system",
                "notificationsEnabled" to true
            )
        ).await()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun forgot(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun currentUserId(): String? = auth.currentUser?.uid
    fun logout() = auth.signOut()
}