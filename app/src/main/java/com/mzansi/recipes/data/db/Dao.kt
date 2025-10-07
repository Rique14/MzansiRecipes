package com.mzansi.recipes.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title")
    fun observeAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE category = :categoryName ORDER BY title")
    fun observeByCategory(categoryName: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE trending = 1 ORDER BY title")
    fun observeTrending(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeById(id: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes WHERE pendingSync = 1")
    suspend fun getPendingSyncRecipes(): List<RecipeEntity>

    @Query("UPDATE recipes SET isSavedOffline = :isSaved WHERE id = :recipeId")
    suspend fun updateSavedOfflineStatus(recipeId: String, isSaved: Boolean)

    @Query("SELECT * FROM recipes WHERE isSavedOffline = 1 ORDER BY title")
    fun observeSavedRecipes(): Flow<List<RecipeEntity>>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
@Dao
interface ShoppingDao {

    @Query("SELECT * FROM shopping_items WHERE userId = :userId")
    fun observe(userId: String): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ShoppingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)

    @Query("SELECT * FROM shopping_items WHERE userId = :userId AND pendingSync = 1")
    suspend fun pending(userId: String): List<ShoppingItemEntity>

    @Query("UPDATE shopping_items SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
}