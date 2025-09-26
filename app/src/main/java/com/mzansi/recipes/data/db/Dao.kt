package com.mzansi.recipes.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE category = :categoryName ORDER BY title")
    fun observeByCategory(categoryName: String): Flow<List<RecipeEntity>> // Added for offline category view

    @Query("SELECT * FROM recipes WHERE trending = 1")
    fun observeTrending(): Flow<List<RecipeEntity>> // Added for offline trending view

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: String): RecipeEntity?
    
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeById(id: String): Flow<RecipeEntity?> // Added for observing a single recipe

    @Query("SELECT * FROM recipes WHERE pendingSync = 1") // pendingSync uses 1 for true in SQLite
    suspend fun getPendingSyncRecipes(): List<RecipeEntity>

    @Update
    suspend fun update(recipe: RecipeEntity) // Added for updating pendingSync status

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Added for single item upsert
    suspend fun upsert(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes WHERE trending = 1 AND category = 'Trending'")
    suspend fun getOldTrendingItems(): List<RecipeEntity>

    @Query("UPDATE recipes SET isSavedOffline = :isSaved WHERE id = :recipeId")
    suspend fun updateSavedOfflineStatus(recipeId: String, isSaved: Boolean) // <<< NEW METHOD

    @Query("SELECT * FROM recipes WHERE isSavedOffline = 1 ORDER BY title")
    fun observeSavedRecipes(): Flow<List<RecipeEntity>> // <<< NEW METHOD
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items WHERE userId = :userId ORDER BY id DESC")
    fun observe(userId: String): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ShoppingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ShoppingItemEntity>)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Query("SELECT * FROM shopping_items WHERE pendingSync = 1 AND userId = :userId")
    suspend fun pending(userId: String): List<ShoppingItemEntity>

    @Query("UPDATE shopping_items SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("SELECT COUNT(*) FROM categories") // Helper to check if categories are cached
    suspend fun count(): Int
}