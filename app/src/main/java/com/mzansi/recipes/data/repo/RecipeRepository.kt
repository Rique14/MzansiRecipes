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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
        private const val FEATURED_CATEGORY_NAME = "Dessert" // Moved to companion object
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
                        name = apiCategory.strCategory,
                        imageUrl = apiCategory.strCategoryThumb,
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

        // Step 1: Demote old items marked with category "Trending"
        try {
            val oldTrendingItems = recipeDao.getOldTrendingItems() // Uses the new DAO method
            if (oldTrendingItems.isNotEmpty()) {
                Log.d(TAG, "Found ${oldTrendingItems.size} old trending items with category 'Trending'. Demoting them.")
                val demotedItems = oldTrendingItems.map { it.copy(trending = false) }
                recipeDao.upsertAll(demotedItems)
                Log.d(TAG, "Demoted old trending items.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error demoting old trending items: ${e.message}", e)
            // Continue with fetching new trending items even if demotion fails for some reason
        }


        // FEATURED_CATEGORY_NAME now a class constant
        Log.d(TAG, "triggerTrendingRecipesRefresh: Fetching recipes from category '$FEATURED_CATEGORY_NAME' to mark as trending.")
        try {
            val response = service.filterByCategory(FEATURED_CATEGORY_NAME)
            val trendingRecipeEntities = response.meals?.take(10)?.mapNotNull { mealSummary ->

                val existing = recipeDao.getById(mealSummary.idMeal)
                RecipeEntity(
                    id = mealSummary.idMeal,
                    title = mealSummary.strMeal,
                    imageUrl = mealSummary.strMealThumb,
                    category = FEATURED_CATEGORY_NAME, // Ensure correct category
                    trending = true,                 // Mark as trending
                    instructions = existing?.instructions,
                    area = existing?.area,
                    pendingSync = existing?.pendingSync ?: false
                )
            } ?: emptyList()

            if (trendingRecipeEntities.isNotEmpty()) {
                Log.d(TAG, "Upserting ${trendingRecipeEntities.size} recipes from category '$FEATURED_CATEGORY_NAME' as trending.")
                recipeDao.upsertAll(trendingRecipeEntities) // Upsert will update if exists, or insert if new
            } else {
                Log.d(TAG, "No recipes found from category '$FEATURED_CATEGORY_NAME' to mark as trending.")
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException during triggerTrendingRecipesRefresh for category '$FEATURED_CATEGORY_NAME': ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "General Exception during triggerTrendingRecipesRefresh for category '$FEATURED_CATEGORY_NAME': ${e.message}", e)
        }
    }

    // --- Recipes by Category --- 

    fun observeRecipesByCategory(categoryName: String): Flow<List<RecipeEntity>> {
        return recipeDao.observeByCategory(categoryName)
    }

    suspend fun refreshRecipesForCategory(categoryName: String) {
         if (categoryName.equals("Trending", ignoreCase = true)) {
             Log.w(TAG, "refreshRecipesForCategory was called with 'Trending'. This usually shouldn't happen via category selection UI.")
             return
         }
         if (networkMonitor.isOnline.first()) {
            try {
                val response = service.filterByCategory(categoryName)
                val recipes = response.meals?.mapNotNull { mealSummary ->
                    val existing = recipeDao.getById(mealSummary.idMeal)
                    RecipeEntity(
                        id = mealSummary.idMeal,
                        title = mealSummary.strMeal,
                        imageUrl = mealSummary.strMealThumb,
                        category = categoryName, 
                        trending = existing?.trending ?: false, 
                        instructions = existing?.instructions, 
                        area = existing?.area,
                        pendingSync = existing?.pendingSync ?: false
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
        if (searchTerm.isBlank()) return emptyList()
        if (!networkMonitor.isOnline.first()) {
            Log.d(TAG, "searchRecipesByName: Offline, returning empty list.")
            return emptyList() 
        }
        return try {
            val response = service.searchByName(searchTerm)
            response.meals?.mapNotNull { mealSummary -> 
                val existing = recipeDao.getById(mealSummary.idMeal) 
                RecipeEntity(
                    id = mealSummary.idMeal,
                    title = mealSummary.strMeal,
                    imageUrl = mealSummary.strMealThumb,
                    category = existing?.category, 
                    trending = existing?.trending ?: false, 
                    instructions = existing?.instructions,
                    area = existing?.area,
                    pendingSync = existing?.pendingSync ?: false
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
        Log.d(TAG, "fetchAndCacheFullRecipeDetails: Called for recipeId: $recipeId")
        val localEntity = recipeDao.getById(recipeId)
        Log.d(TAG, "fetchAndCacheFullRecipeDetails: Local entity for $recipeId: Title='${localEntity?.title}', Area: ${localEntity?.area}, Category: ${localEntity?.category}")

        if (networkMonitor.isOnline.first()) {
            Log.d(TAG, "fetchAndCacheFullRecipeDetails: Device is online. Attempting network fetch for $recipeId.")
            try {
                val response = service.lookupRecipeById(recipeId)
                Log.d(TAG, "fetchAndCacheFullRecipeDetails: API response for $recipeId: meals list is null? ${response.meals == null}")
                val networkDetail = response.meals?.firstOrNull()
                
                if (networkDetail != null) {
                    Log.d(TAG, "fetchAndCacheFullRecipeDetails: Network detail found for $recipeId: '${networkDetail.strMeal}', Area: ${networkDetail.strArea}, Category: ${networkDetail.strCategory}")
                    val ingredients = formatIngredients(networkDetail)
                    
                    val bestCategory = networkDetail.strCategory?.takeIf { it.isNotBlank() } ?: localEntity?.category
                    
                    val updatedEntity = (localEntity ?: RecipeEntity(
                        id = networkDetail.idMeal,
                        title = networkDetail.strMeal,
                        imageUrl = networkDetail.strMealThumb,
                        category = bestCategory, 
                        trending = localEntity?.trending ?: true, 
                        pendingSync = localEntity?.pendingSync ?: false 
                    )).copy(
                        instructions = networkDetail.strInstructions ?: localEntity?.instructions,
                        area = networkDetail.strArea ?: localEntity?.area,
                        title = networkDetail.strMeal, 
                        imageUrl = networkDetail.strMealThumb,
                        category = bestCategory,
                        // Ensure trending status from localEntity is preserved if it exists, especially if it was part of the FEATURED_CATEGORY_NAME
                        trending = localEntity?.trending ?: (bestCategory == FEATURED_CATEGORY_NAME) // Use class constant
                    )
                    Log.d(TAG, "fetchAndCacheFullRecipeDetails: Upserting entity for $recipeId with Title='${updatedEntity.title}', Area: ${updatedEntity.area}, Category: ${updatedEntity.category}, Trending: ${updatedEntity.trending}")
                    recipeDao.upsert(updatedEntity)
                    
                    val fullDetails = RecipeFullDetails(
                        id = updatedEntity.id,
                        title = updatedEntity.title,
                        imageUrl = updatedEntity.imageUrl,
                        instructions = updatedEntity.instructions,
                        category = updatedEntity.category,
                        area = updatedEntity.area,
                        ingredients = ingredients
                    )
                    Log.d(TAG, "fetchAndCacheFullRecipeDetails: Returning full details from network for $recipeId: Title='${fullDetails.title}', Area: ${fullDetails.area}, Category: ${fullDetails.category}")
                    return fullDetails
                } else {
                    Log.d(TAG, "fetchAndCacheFullRecipeDetails: Network detail was null for $recipeId even after API call.")
                }
            } catch (e: IOException) {
                Log.e(TAG, "IOException during fetchAndCacheFullRecipeDetails for $recipeId: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "General Exception during fetchAndCacheFullRecipeDetails for $recipeId: ${e.message}", e)
            }
        }
        Log.d(TAG, "fetchAndCacheFullRecipeDetails: Returning local fallback for $recipeId. Local entity was: Title='${localEntity?.title}', Category='${localEntity?.category}'")
        return localEntity?.let {
            RecipeFullDetails(
                id = it.id,
                title = it.title,
                imageUrl = it.imageUrl,
                instructions = it.instructions,
                category = it.category, 
                area = it.area,
                ingredients = emptyList()
            )
        }
    }

    // --- Saved Recipes for Offline --- NEW SECTION

    suspend fun saveRecipeForOffline(recipeId: String) {
        // Ensure full details are present in the database first
        // This also ensures the recipe exists and its latest details are cached.
        fetchAndCacheFullRecipeDetails(recipeId)
        // Now mark it as saved
        recipeDao.updateSavedOfflineStatus(recipeId, true)
        Log.d(TAG, "Recipe $recipeId marked as saved for offline.")
    }

    suspend fun unsaveRecipe(recipeId: String) {
        recipeDao.updateSavedOfflineStatus(recipeId, false)
        Log.d(TAG, "Recipe $recipeId unmarked from offline saved.")
    }

    fun observeSavedRecipes(): Flow<List<RecipeEntity>> {
        Log.d(TAG, "Observing saved recipes.")
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