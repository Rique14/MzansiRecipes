package com.mzansi.recipes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String?,
    val category: String?,
    val trending: Boolean = false,
    val pendingSync: Boolean = false,
    val instructions: String? = null,
    val area: String? = null,
    val isSavedOffline: Boolean = false // <<< NEW FIELD
)

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val ingredientId: Long = 0,
    val recipeId: String,
    val name: String,
    val quantity: String?
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val itemName: String,
    val isChecked: Boolean = false,
    val originRecipeId: String? = null,
    val pendingSync: Boolean = false
)