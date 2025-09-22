package com.mzansi.recipes.data.repo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class CommunityPost(
    val postId: String = "",
    val userId: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val likes: Int = 0,
    val category: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class CommunityRepository(
    private val fs: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val col get() = fs.collection("community")

    suspend fun listPopular(): List<CommunityPost> {
        val snapshot = col.orderBy("likes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20).get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                CommunityPost(
                    postId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    title = doc.getString("title") ?: "No title",
                    imageUrl = doc.getString("imageUrl"),
                    likes = doc.getLong("likes")?.toInt() ?: 0,
                    category = doc.getString("category"),
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            } catch (e: Exception) {
                Log.e("CommunityRepository", "Error mapping document ${doc.id}", e)
                null
            }
        }
    }

    suspend fun like(postId: String) {
        val ref = col.document(postId)
        fs.runTransaction { txn ->
            val snap = txn.get(ref)
            val likes = (snap.getLong("likes") ?: 0L) + 1
            txn.update(ref, "likes", likes)
        }.await()
    }

    suspend fun create(title: String, imageUrl: String? = null, category: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        col.add(
            CommunityPost(userId = uid, title = title, imageUrl = imageUrl, category = category)
        ).await()
    }
}