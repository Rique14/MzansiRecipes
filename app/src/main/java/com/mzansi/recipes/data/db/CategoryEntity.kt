package com.mzansi.recipes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String, // Assuming idCategory from API maps to this
    val name: String,           // Assuming strCategory from API maps to this
    val imageUrl: String?,      // Assuming strCategoryThumb from API maps to this
    val description: String?    // Assuming strCategoryDescription from API maps to this
)
