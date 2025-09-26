package com.mzansi.recipes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeEntity::class, IngredientEntity::class, ShoppingItemEntity::class, CategoryEntity::class],
    version = 6 // Incremented version due to schema change (added isSavedOffline)
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun categoryDao(): CategoryDao
}