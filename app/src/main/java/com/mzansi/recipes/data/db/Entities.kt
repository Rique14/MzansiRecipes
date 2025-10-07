package com.mzansi.recipes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

// --- Type Converters ---
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> = value?.split(",")?.map { it.trim() } ?: emptyList()

    @TypeConverter
    fun fromList(list: List<String>?): String = list?.joinToString(",") ?: ""
}

// --- Entities ---
@Entity(tableName = "recipes")
@TypeConverters(Converters::class)
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String? = null,
    val instructions: String? = null,
    val category: String,
    val area: String? = null,
    val trending: Boolean = false,
    var isSavedOffline: Boolean = false,
    val pendingSync: Boolean = false,
    val ingredients: List<String> = emptyList()
)


@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val itemName: String,
    var isChecked: Boolean = false,
    val originRecipeId: String? = null,
    val pendingSync: Boolean = true
)
