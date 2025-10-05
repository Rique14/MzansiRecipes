package com.mzansi.recipes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RecipeEntity::class, CategoryEntity::class, ShoppingItemEntity::class], version = 1)
@TypeConverters(Converters::class) // Add this line to register the type converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun shoppingDao(): ShoppingDao
}
