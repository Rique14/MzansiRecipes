package com.mzansi.recipes.data.repo

import android.util.Log
import com.mzansi.recipes.data.api.TastyService
import com.mzansi.recipes.data.db.RecipeDao
import com.mzansi.recipes.data.db.RecipeEntity

data class RecipeFullDetails(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val prepTime: Int?,
    val servings: Int?,
    val ingredients: List<String>
)

class RecipeRepository(
    private val service: TastyService,
    private val recipeDao: RecipeDao
) {
    suspend fun refreshTrending(): List<RecipeEntity> {
        val response = service.listRecipes(size = 40) // Increased size to get more recipes
        val allRecipes = mutableListOf<RecipeEntity>()

        // The API returns a mix of articles ("buzzes") and actual recipes ("tasty-search").
        // We will only use the tasty-search items as they are guaranteed to be recipes.
        response.data?.sections?.forEach { section ->
            section.items?.tastySearch?.mapNotNullTo(allRecipes) { tasty ->
                val id = tasty.id?.toString() ?: return@mapNotNullTo null
                RecipeEntity(
                    id = id,
                    title = tasty.name ?: "Untitled",
                    imageUrl = tasty.thumbnailUrl,
                    prepTime = null, // This info isn't in the list view
                    servings = null, // This info isn't in the list view
                    category = "Trending",
                    trending = true
                )
            }
        }

        if (allRecipes.isNotEmpty()) {
            recipeDao.upsertAll(allRecipes)
        }
        return allRecipes
    }

    suspend fun getFullDetail(id: String): RecipeFullDetails {
        val detail = service.getRecipeDetail(id)
        return RecipeFullDetails(
            id = detail.id ?: id,
            title = detail.name ?: "Recipe",
            imageUrl = detail.thumbnailUrl,
            prepTime = detail.cook_time_minutes,
            servings = detail.num_servings,
            ingredients = detail.sections?.flatMap { it.components ?: emptyList() }
                ?.mapNotNull { it.raw_text }.orEmpty()
        )
    }

    suspend fun getDetail(id: String): RecipeEntity? = recipeDao.getById(id)
}
