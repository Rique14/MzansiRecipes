package com.mzansi.recipes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String, 
    val name: String,
    val imageUrl: String, // Corrected: This should not be nullable
    val description: String?
)
