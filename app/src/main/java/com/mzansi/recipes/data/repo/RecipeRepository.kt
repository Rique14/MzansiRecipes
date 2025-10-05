package com.mzansi.recipes.data.repo

import android.util.Log
import com.mzansi.recipes.data.api.MealDbService
import com.mzansi.recipes.data.api.MealDetail
import com.mzansi.recipes.data.db.CategoryDao
import com.mzansi.recipes.data.db.CategoryEntity
import com.mzansi.recipes.data.db.RecipeDao
import com.mzansi.recipes.data.db.RecipeEntity
import com.mzansi.recipes.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

// Data class for combined recipe details, including ingredients
data class RecipeFullDetails(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val instructions: String?,
    val category: String?,
    val area: String?,
    val ingredients: List<String> // Combined ingredient and measure strings
)

class RecipeRepository(
    private val service: MealDbService,
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao,
    private val networkMonitor: NetworkMonitor
) {
    private val TAG = "RecipeRepository" // Logging Tag

    companion object {
        private const val FEATURED_CATEGORY_NAME = "Dessert"
    }

    // --- Categories --- 

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun refreshCategoriesIfNeeded() {
        val categoriesCount = categoryDao.count()
        if (categoriesCount == 0 && networkMonitor.isOnline.first()) {
            try {
                val response = service.listCategories()
                val categoryEntities = response.categories?.map { apiCategory ->
                    CategoryEntity(
                        id = apiCategory.idCategory,
                        name = apiCategory.strCategory ?: "",
                        imageUrl = apiCategory.strCategoryThumb ?: "",
                        description = apiCategory.strCategoryDescription
                    )
                } ?: emptyList()
                if (categoryEntities.isNotEmpty()) {
                    categoryDao.upsertAll(categoryEntities)
                }
            } catch (e: IOException) {
                Log.e(TAG, "IOException during refreshCategoriesIfNeeded: ${e.message}")
            }
        }
    }

    // --- Trending Recipes (now sourced from a specific category) ---

    fun observeTrendingRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeTrending()

    suspend fun triggerTrendingRecipesRefresh() {
        if (!networkMonitor.isOnline.first()) {
            Log.d(TAG, "triggerTrendingRecipesRefresh: Offline, skipping refresh.")
            return
        }

        try {
            val oldTrendingItems = recipeDao.getOldTrendingItems()
            if (oldTrendingItems.isNotEmpty()) {
                val demotedItems = oldTrendingItems.map { it.copy(trending = false) }
                recipeDao.upsertAll(demotedItems)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error demoting old trending items: ${e.message}", e)
        }

        Log.d(TAG, "triggerTrendingRecipesRefresh: Fetching recipes from category '$FEATURED_CATEGORY_NAME' to mark as trending.")
        try {
            val response = service.filterByCategory(FEATURED_CATEGORY_NAME)
            val trendingRecipeEntities = response.meals?.take(10)?.mapNotNull { mealSummary ->
                val existing = recipeDao.getById(mealSummary.idMeal)
                RecipeEntity(
                    id = mealSummary.idMeal,
                    title = mealSummary.strMeal,
                    imageUrl = mealSummary.strMealThumb ?: "",
                    category = FEATURED_CATEGORY_NAME,
                    trending = true,
                    instructions = existing?.instructions ?: "",
                    area = existing?.area ?: "",
                    pendingSync = existing?.pendingSync ?: false,
                    isSavedOffline = existing?.isSavedOffline ?: false
                )
            } ?: emptyList()

            if (trendingRecipeEntities.isNotEmpty()) {
                recipeDao.upsertAll(trendingRecipeEntities)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during triggerTrendingRecipesRefresh: ${e.message}", e)
        }
    }

    // --- Recipes by Category --- 

    fun observeRecipesByCategory(categoryName: String): Flow<List<RecipeEntity>> {
        return recipeDao.observeByCategory(categoryName)
    }

    suspend fun refreshRecipesForCategory(categoryName: String) {
        if (categoryName.equals("Trending", ignoreCase = true)) return
        if (networkMonitor.isOnline.first()) {
            try {
                val response = service.filterByCategory(categoryName)
                val recipes = response.meals?.mapNotNull { mealSummary ->
                    val existing = recipeDao.getById(mealSummary.idMeal)
                    RecipeEntity(
                        id = mealSummary.idMeal,
                        title = mealSummary.strMeal,
                        imageUrl = mealSummary.strMealThumb ?: "",
                        category = categoryName,
                        trending = existing?.trending ?: false,
                        instructions = existing?.instructions ?: "",
                        area = existing?.area ?: "",
                        pendingSync = existing?.pendingSync ?: false,
                        isSavedOffline = existing?.isSavedOffline ?: false
                    )
                } ?: emptyList()
                if (recipes.isNotEmpty()) {
                    recipeDao.upsertAll(recipes)
                }
            } catch (e: IOException) {
                Log.e(TAG, "IOException during refreshRecipesForCategory $categoryName: ${e.message}")
            }
        }
    }

    // --- Search --- 

    suspend fun searchRecipesByName(searchTerm: String): List<RecipeEntity> {
        if (searchTerm.isBlank() || !networkMonitor.isOnline.first()) return emptyList()
        return try {
            val response = service.searchByName(searchTerm)
            response.meals?.mapNotNull { mealSummary ->
                val existing = recipeDao.getById(mealSummary.idMeal)
                RecipeEntity(
                    id = mealSummary.idMeal,
                    title = mealSummary.strMeal,
                    imageUrl = mealSummary.strMealThumb ?: "",
                    category = existing?.category ?: "", // Provide a fallback for category
                    trending = existing?.trending ?: false,
                    instructions = existing?.instructions ?: "",
                    area = existing?.area ?: "",
                    pendingSync = existing?.pendingSync ?: false,
                    isSavedOffline = existing?.isSavedOffline ?: false
                )
            } ?: emptyList()
        } catch (e: IOException) {
            Log.e(TAG, "IOException during searchRecipesByName: ${e.message}")
            emptyList()
        }
    }

    // --- Full Recipe Detail --- 

    fun observeRecipeById(recipeId: String): Flow<RecipeEntity?> = recipeDao.observeById(recipeId)

    suspend fun fetchAndCacheFullRecipeDetails(recipeId: String): RecipeFullDetails? {
        val localEntity = recipeDao.getById(recipeId)

        // FIX: Only use local data if ingredients are meaningfully cached.
        if (localEntity != null && localEntity.isSavedOffline && localEntity.ingredients.any { it.isNotBlank() }) {
            return RecipeFullDetails(
                id = localEntity.id,
                title = localEntity.title,
                imageUrl = localEntity.imageUrl,
                instructions = localEntity.instructions,
                category = localEntity.category,
                area = localEntity.area,
                ingredients = localEntity.ingredients
            )
        }

        if (networkMonitor.isOnline.first()) {
            try {
                val response = service.lookupRecipeById(recipeId)
                val networkDetail = response.meals?.firstOrNull()
                
                if (networkDetail != null) {
                    val ingredients = formatIngredients(networkDetail)
                    val bestCategory = networkDetail.strCategory?.takeIf { it.isNotBlank() } ?: localEntity?.category
                    
                    val updatedEntity = (localEntity ?: RecipeEntity(id = networkDetail.idMeal, title = networkDetail.strMeal, imageUrl = networkDetail.strMealThumb ?: "", category = "", instructions = "", area = "")).copy(
                        instructions = networkDetail.strInstructions ?: localEntity?.instructions ?: "",
                        area = networkDetail.strArea ?: localEntity?.area ?: "",
                        title = networkDetail.strMeal, 
                        imageUrl = networkDetail.strMealThumb ?: "",
                        category = bestCategory ?: "",
                        trending = localEntity?.trending ?: (bestCategory == FEATURED_CATEGORY_NAME),
                        ingredients = ingredients // Save ingredients to the database
                    )
                    recipeDao.upsert(updatedEntity)
                    
                    return RecipeFullDetails(
                        id = updatedEntity.id,
                        title = updatedEntity.title,
                        imageUrl = updatedEntity.imageUrl,
                        instructions = updatedEntity.instructions,
                        category = updatedEntity.category,
                        area = updatedEntity.area,
                        ingredients = ingredients
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during fetchAndCacheFullRecipeDetails for $recipeId: ${e.message}", e)
            }
        }

        // Fallback for when API fails but we still have some local data.
        return localEntity?.let {
            RecipeFullDetails(
                id = it.id,
                title = it.title,
                imageUrl = it.imageUrl,
                instructions = it.instructions,
                category = it.category,
                area = it.area,
                ingredients = it.ingredients.filter { i -> i.isNotBlank() } // Clean up bogus ingredients
            )
        }
    }

    // --- Saved Recipes for Offline ---

    suspend fun saveRecipeEntity(recipe: RecipeEntity) {
        recipeDao.upsert(recipe)
    }

    suspend fun isRecipeSavedOffline(recipeId: String): Boolean {
        return recipeDao.getById(recipeId)?.isSavedOffline ?: false
    }

    suspend fun saveRecipeForOffline(recipeId: String) {
        fetchAndCacheFullRecipeDetails(recipeId)
        recipeDao.updateSavedOfflineStatus(recipeId, true)
    }

    suspend fun removeRecipeFromOffline(recipeId: String) {
        recipeDao.updateSavedOfflineStatus(recipeId, false)
    }

    fun observeSavedRecipes(): Flow<List<RecipeEntity>> {
        return recipeDao.observeSavedRecipes()
    }

    private fun formatIngredients(detail: MealDetail): List<String> {
        val ingredients = mutableListOf<String>()
        val ingredientList = listOf(
            detail.strIngredient1, detail.strIngredient2, detail.strIngredient3, detail.strIngredient4, detail.strIngredient5,
            detail.strIngredient6, detail.strIngredient7, detail.strIngredient8, detail.strIngredient9, detail.strIngredient10,
            detail.strIngredient11, detail.strIngredient12, detail.strIngredient13, detail.strIngredient14, detail.strIngredient15,
            detail.strIngredient16, detail.strIngredient17, detail.strIngredient18, detail.strIngredient19, detail.strIngredient20
        )
        val measureList = listOf(
            detail.strMeasure1, detail.strMeasure2, detail.strMeasure3, detail.strMeasure4, detail.strMeasure5,
            detail.strMeasure6, detail.strMeasure7, detail.strMeasure8, detail.strMeasure9, detail.strMeasure10,
            detail.strMeasure11, detail.strMeasure12, detail.strMeasure13, detail.strMeasure14, detail.strMeasure15,
            detail.strMeasure16, detail.strMeasure17, detail.strMeasure18, detail.strMeasure19, detail.strMeasure20
        )

        for (i in 0..19) {
            val ingredient = ingredientList[i]
            val measure = measureList[i]
            if (!ingredient.isNullOrBlank()) {
                ingredients.add("${measure?.trim() ?: ""} ${ingredient.trim()}".trim())
            }
        }
        return ingredients
    }
}
