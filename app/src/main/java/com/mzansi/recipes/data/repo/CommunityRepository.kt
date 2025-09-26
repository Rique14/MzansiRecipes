package com.mzansi.recipes.data.repo

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CommunityPost(
    val postId: String = "",
    val userId: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val ingredients: String = "",
    val instructions: String = "",
    val likes: Int = 0,
    val category: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceApiId: String? = null, // To link to original API recipe if applicable
    val isUserUploaded: Boolean = true // True for user posts, false for API-sourced posts
)

class CommunityRepository(
    private val fs: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
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
                    ingredients = doc.getString("ingredients") ?: "",
                    instructions = doc.getString("instructions") ?: "",
                    likes = doc.getLong("likes")?.toInt() ?: 0,
                    category = doc.getString("category"),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    sourceApiId = doc.getString("sourceApiId"),
                    isUserUploaded = doc.getBoolean("isUserUploaded") ?: true
                )
            } catch (e: Exception) {
                Log.e("CommunityRepository", "Error mapping document ${doc.id} in listPopular", e)
                null
            }
        }
    }

    fun getPostById(postId: String): Flow<CommunityPost?> {
        return col.document(postId).snapshots().map { doc ->
            if (doc.exists()) {
                try {
                    CommunityPost(
                        postId = doc.id,
                        userId = doc.getString("userId") ?: "",
                        title = doc.getString("title") ?: "No title",
                        imageUrl = doc.getString("imageUrl"),
                        ingredients = doc.getString("ingredients") ?: "",
                        instructions = doc.getString("instructions") ?: "",
                        likes = doc.getLong("likes")?.toInt() ?: 0,
                        category = doc.getString("category"),
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        sourceApiId = doc.getString("sourceApiId"),
                        isUserUploaded = doc.getBoolean("isUserUploaded") ?: true
                    )
                } catch (e: Exception) {
                    Log.e("CommunityRepository", "Error mapping document ${doc.id} in getPostById", e)
                    null // Error during deserialization
                }
            } else {
                null // Document does not exist
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

    suspend fun create(title: String, imageUri: Uri?, ingredients: String, instructions: String, category: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        var finalImageUrl: String? = null

        if (imageUri != null) {
            try {
                val imageRef = storage.reference.child("community_images/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).await()
                finalImageUrl = imageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.e("CommunityRepository", "Error uploading image", e)
            }
        }

        val newPostRef = col.document() // Auto-generate ID
        val post = CommunityPost(
            postId = newPostRef.id, // Set the postId to the document's ID
            userId = uid,
            title = title,
            imageUrl = finalImageUrl,
            ingredients = ingredients,
            instructions = instructions,
            category = category,
            isUserUploaded = true
        )
        newPostRef.set(post).await()
    }

    suspend fun findBySourceApiId(apiId: String): CommunityPost? {
        val snapshot = col.whereEqualTo("sourceApiId", apiId).limit(1).get().await()
        return snapshot.documents.firstOrNull()?.let { doc ->
            try {
                CommunityPost(
                    postId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    title = doc.getString("title") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    ingredients = doc.getString("ingredients") ?: "",
                    instructions = doc.getString("instructions") ?: "",
                    likes = doc.getLong("likes")?.toInt() ?: 0,
                    category = doc.getString("category"),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    sourceApiId = doc.getString("sourceApiId"),
                    isUserUploaded = doc.getBoolean("isUserUploaded") ?: false
                )
            } catch (e: Exception) {
                Log.e("CommunityRepository", "Error mapping document ${doc.id} in findBySourceApiId", e)
                null
            }
        }
    }

    suspend fun createOrGetApiRecipePost(apiRecipeId: String, title: String, imageUrl: String?, category: String?, initialIngredients: String = "", initialInstructions: String = ""): String {
        val existingPost = findBySourceApiId(apiRecipeId)
        if (existingPost != null) {
            return existingPost.postId
        }

        val newPostRef = col.document() // Auto-generate ID
        val communityPost = CommunityPost(
            postId = newPostRef.id,
            userId = "API_SOURCE",
            title = title,
            imageUrl = imageUrl,
            ingredients = initialIngredients,
            instructions = initialInstructions,
            likes = 0,
            category = category,
            isUserUploaded = false,
            sourceApiId = apiRecipeId,
            timestamp = System.currentTimeMillis()
        )
        newPostRef.set(communityPost).await()
        return newPostRef.id
    }
}