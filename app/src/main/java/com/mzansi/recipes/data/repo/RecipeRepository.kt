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

data class RecipeFullDetails(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val instructions: String?,
    val category: String?,
    val area: String?,
    val ingredients: List<String>
)

class RecipeRepository(
    private val service: MealDbService,
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao,
    private val networkMonitor: NetworkMonitor
) {
    private val TAG = "RecipeRepository"

    // --- Categories ---
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun refreshCategoriesIfNeeded() {
        val count = categoryDao.count()
        if (count == 0 && networkMonitor.isOnline.first()) {
            try {
                val response = service.listCategories()
                val categories = response.categories?.map { api ->
                    CategoryEntity(
                        id = api.idCategory,
                        name = api.strCategory ?: "",
                        imageUrl = api.strCategoryThumb ?: "",
                        description = api.strCategoryDescription
                    )
                } ?: emptyList()
                if (categories.isNotEmpty()) categoryDao.upsertAll(categories)
            } catch (e: IOException) {
                Log.e(TAG, "IOException in refreshCategoriesIfNeeded: ${e.message}")
            }
        }
    }

    // --- Observe all recipes ---
    fun observeAllRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeAllRecipes()

    // --- Trending Recipes ---
    fun observeTrendingRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeTrending()

    suspend fun triggerTrendingRecipesRefresh() {
        if (!networkMonitor.isOnline.first()) return
        try {
            // Demote old trending
            val oldTrending = recipeDao.observeTrending().first()
            if (oldTrending.isNotEmpty()) {
                recipeDao.upsertAll(oldTrending.map { it.copy(trending = false) })
            }

            // Pick random trending from categories
            val categories = service.listCategories().categories?.mapNotNull { it.strCategory } ?: emptyList()
            val randomCategories = categories.shuffled().take(3)
            val newTrending = mutableListOf<RecipeEntity>()

            for (cat in randomCategories) {
                val meals = service.filterByCategory(cat).meals?.shuffled()?.take(2) ?: emptyList()
                for (meal in meals) {
                    val existing = recipeDao.getById(meal.idMeal)
                    newTrending.add(
                        RecipeEntity(
                            id = meal.idMeal,
                            title = meal.strMeal,
                            imageUrl = meal.strMealThumb ?: "",
                            category = cat,
                            trending = true,
                            instructions = existing?.instructions ?: "",
                            area = existing?.area ?: "",
                            pendingSync = existing?.pendingSync ?: false,
                            isSavedOffline = existing?.isSavedOffline ?: false
                        )
                    )
                }
            }
            if (newTrending.isNotEmpty()) recipeDao.upsertAll(newTrending)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing trending recipes: ${e.message}", e)
        }
    }

    // --- Recipes by category ---
    fun observeRecipesByCategory(categoryName: String): Flow<List<RecipeEntity>> =
        recipeDao.observeByCategory(categoryName)

    suspend fun refreshRecipesForCategory(categoryName: String) {
        if (categoryName.equals("Trending", true)) return
        if (!networkMonitor.isOnline.first()) return

        try {
            val response = service.filterByCategory(categoryName)
            val recipes = response.meals?.mapNotNull { meal ->
                val existing = recipeDao.getById(meal.idMeal)
                RecipeEntity(
                    id = meal.idMeal,
                    title = meal.strMeal,
                    imageUrl = meal.strMealThumb ?: "",
                    category = categoryName,
                    trending = existing?.trending ?: false,
                    instructions = existing?.instructions ?: "",
                    area = existing?.area ?: "",
                    pendingSync = existing?.pendingSync ?: false,
                    isSavedOffline = existing?.isSavedOffline ?: false
                )
            } ?: emptyList()

            if (recipes.isNotEmpty()) recipeDao.upsertAll(recipes)
        } catch (e: IOException) {
            Log.e(TAG, "IOException refreshing category $categoryName: ${e.message}")
        }
    }

    // --- Search ---
    suspend fun searchRecipesByName(searchTerm: String): List<RecipeEntity> {
        if (searchTerm.isBlank() || !networkMonitor.isOnline.first()) return emptyList()
        return try {
            val response = service.searchByName(searchTerm)
            response.meals?.mapNotNull { meal ->
                val existing = recipeDao.getById(meal.idMeal)
                RecipeEntity(
                    id = meal.idMeal,
                    title = meal.strMeal,
                    imageUrl = meal.strMealThumb ?: "",
                    category = existing?.category ?: "",
                    trending = existing?.trending ?: false,
                    instructions = existing?.instructions ?: "",
                    area = existing?.area ?: "",
                    pendingSync = existing?.pendingSync ?: false,
                    isSavedOffline = existing?.isSavedOffline ?: false
                )
            } ?: emptyList()
        } catch (e: IOException) {
            Log.e(TAG, "IOException searchRecipesByName: ${e.message}")
            emptyList()
        }
    }

    // --- Single recipe / full detail ---
    fun observeRecipeById(recipeId: String): Flow<RecipeEntity?> =
        recipeDao.observeById(recipeId)

    suspend fun fetchAndCacheFullRecipeDetails(recipeId: String): RecipeFullDetails? {
        val local = recipeDao.getById(recipeId)
        if (local != null && local.isSavedOffline && local.ingredients.any { it.isNotBlank() }) {
            return RecipeFullDetails(local.id, local.title, local.imageUrl, local.instructions, local.category, local.area, local.ingredients)
        }

        if (networkMonitor.isOnline.first()) {
            try {
                val detail = service.lookupRecipeById(recipeId).meals?.firstOrNull()
                if (detail != null) {
                    val ingredients = formatIngredients(detail)
                    val updated = (local ?: RecipeEntity(recipeId, detail.strMeal, detail.strMealThumb ?: "", "", detail.strInstructions ?: "", detail.strArea ?: "")).copy(
                        instructions = detail.strInstructions ?: local?.instructions ?: "",
                        area = detail.strArea ?: local?.area ?: "",
                        title = detail.strMeal,
                        imageUrl = detail.strMealThumb ?: "",
                        category = detail.strCategory ?: local?.category ?: "",
                        ingredients = ingredients
                    )
                    recipeDao.upsert(updated)
                    return RecipeFullDetails(updated.id, updated.title, updated.imageUrl, updated.instructions, updated.category, updated.area, ingredients)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching full details for $recipeId: ${e.message}", e)
            }
        }

        return local?.let { RecipeFullDetails(it.id, it.title, it.imageUrl, it.instructions, it.category, it.area, it.ingredients.filter { i -> i.isNotBlank() }) }
    }

    // --- Saved / Offline ---
    suspend fun saveRecipeEntity(recipe: RecipeEntity) = recipeDao.upsert(recipe)
    suspend fun isRecipeSavedOffline(recipeId: String) = recipeDao.getById(recipeId)?.isSavedOffline ?: false
    suspend fun saveRecipeForOffline(recipeId: String) {
        fetchAndCacheFullRecipeDetails(recipeId)
        recipeDao.updateSavedOfflineStatus(recipeId, true)
    }
    suspend fun removeRecipeFromOffline(recipeId: String) = recipeDao.updateSavedOfflineStatus(recipeId, false)
    fun observeSavedRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeSavedRecipes()

    // --- Helper: Ingredients formatting ---
    private fun formatIngredients(detail: MealDetail): List<String> {
        val ingredients = mutableListOf<String>()
        val ingList = listOf(
            detail.strIngredient1, detail.strIngredient2, detail.strIngredient3, detail.strIngredient4, detail.strIngredient5,
            detail.strIngredient6, detail.strIngredient7, detail.strIngredient8, detail.strIngredient9, detail.strIngredient10,
            detail.strIngredient11, detail.strIngredient12, detail.strIngredient13, detail.strIngredient14, detail.strIngredient15,
            detail.strIngredient16, detail.strIngredient17, detail.strIngredient18, detail.strIngredient19, detail.strIngredient20
        )
        val measList = listOf(
            detail.strMeasure1, detail.strMeasure2, detail.strMeasure3, detail.strMeasure4, detail.strMeasure5,
            detail.strMeasure6, detail.strMeasure7, detail.strMeasure8, detail.strMeasure9, detail.strMeasure10,
            detail.strMeasure11, detail.strMeasure12, detail.strMeasure13, detail.strMeasure14, detail.strMeasure15,
            detail.strMeasure16, detail.strMeasure17, detail.strMeasure18, detail.strMeasure19, detail.strMeasure20
        )
        for (i in ingList.indices) {
            val ing = ingList[i]
            val meas = measList[i]
            if (!ing.isNullOrBlank()) ingredients.add("${meas?.trim() ?: ""} ${ing.trim()}".trim())
        }
        return ingredients
    }
}
