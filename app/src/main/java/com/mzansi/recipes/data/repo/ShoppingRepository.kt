package com.mzansi.recipes.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mzansi.recipes.data.db.ShoppingDao
import com.mzansi.recipes.data.db.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ShoppingRepository(
    private val dao: ShoppingDao,
    private val fs: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun observe(): Flow<List<ShoppingItemEntity>> {
        val userId = auth.currentUser?.uid ?: ""
        return dao.observe(userId)
    }

    suspend fun addItem(name: String, originRecipeId: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        dao.upsert(
            ShoppingItemEntity(
                userId = userId, itemName = name,
                originRecipeId = originRecipeId, pendingSync = true
            )
        )
    }

    // Function to add a list of items
    suspend fun addItems(items: List<String>, originRecipeId: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        val entities = items.map {
            ShoppingItemEntity(
                userId = userId,
                itemName = it,
                originRecipeId = originRecipeId,
                pendingSync = true
            )
        }
        dao.upsertAll(entities)
    }


    suspend fun toggleChecked(id: Long, checked: Boolean) = dao.setChecked(id, checked)
    suspend fun delete(item: ShoppingItemEntity) = dao.delete(item)

    // Simple one-way sync to Firestore collection per user
    suspend fun syncPending() {
        val userId = auth.currentUser?.uid ?: return
        val pending = dao.pending(userId)
        if (pending.isEmpty()) return

        val batch = fs.batch()
        val col = fs.collection("shopping").document(userId).collection("items")
        pending.forEach { item ->
            val ref = if (item.id == 0L) col.document() else col.document(item.id.toString())
            batch.set(ref, mapOf(
                "name" to item.itemName,
                "checked" to item.isChecked,
                "originRecipeId" to item.originRecipeId
            ))
        }
        batch.commit().await()
        dao.markSynced(pending.mapNotNull { if (it.id != 0L) it.id else null })
    }
}