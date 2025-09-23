package com.mzansi.recipes.data.repo

import com.mzansi.recipes.data.api.CategorySummary
import com.mzansi.recipes.data.api.MealDbService
import com.mzansi.recipes.data.api.MealDetail
import com.mzansi.recipes.data.db.RecipeDao
import com.mzansi.recipes.data.db.RecipeEntity

// Updated data class to match the details available from TheMealDB
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
    // The service is now MealDbService
    private val service: MealDbService,
    private val recipeDao: RecipeDao
) {
    /**
     * Fetches trending recipes (latest meals)
     * and stores them in the local database.
     */
    suspend fun refreshTrending(): List<RecipeEntity> {
        val response = service.getLatest()
        val allRecipes = response.meals?.map { meal ->
            RecipeEntity(
                id = meal.idMeal,
                title = meal.strMeal,
                imageUrl = meal.strMealThumb,
                prepTime = null, // Not available from this TheMealDB endpoint
                servings = null, // Not available from this TheMealDB endpoint
                category = "Trending", // Keep this for UI consistency
                trending = true
            )
        } ?: emptyList()

        if (allRecipes.isNotEmpty()) {
            recipeDao.upsertAll(allRecipes)
        }
        return allRecipes
    }

    /**
     * Fetches the list of all meal categories from TheMealDB.
     */
    suspend fun getCategories(): List<CategorySummary> {
        val response = service.listCategories()
        return response.categories ?: emptyList()
    }

    /**
     * Searches for recipes by name from TheMealDB.
     * Note: This does not save results to the local database.
     */
    suspend fun searchRecipesByName(searchTerm: String): List<RecipeEntity> {
        if (searchTerm.isBlank()) return emptyList()
        val response = service.searchByName(searchTerm)
        return response.meals?.map { meal ->
            RecipeEntity(
                id = meal.idMeal,
                title = meal.strMeal,
                imageUrl = meal.strMealThumb,
                // These fields might not be available from search results directly
                // Depending on API, might need a subsequent getFullDetail call if needed
                prepTime = null,
                servings = null,
                category = null, // Category might not be part of basic search result
                trending = false
            )
        } ?: emptyList()
    }
    
    /**
     * Fetches recipes by category from TheMealDB.
     * Note: This does not save results to the local database.
     */
    suspend fun getRecipesByCategory(categoryName: String): List<RecipeEntity> {
        if (categoryName.isBlank()) return emptyList()
        val response = service.filterByCategory(categoryName)
        return response.meals?.map { meal ->
            RecipeEntity(
                id = meal.idMeal,
                title = meal.strMeal,
                imageUrl = meal.strMealThumb,
                prepTime = null,
                servings = null,
                category = categoryName, // We know the category we are fetching for
                trending = false
            )
        } ?: emptyList()
    }


    /**
     * Fetches the full details for a single recipe from TheMealDB.
     */
    suspend fun getFullDetail(id: String): RecipeFullDetails? {
        val response = service.lookupRecipeById(id)
        val detail = response.meals.firstOrNull() ?: return null

        return RecipeFullDetails(
            id = detail.idMeal,
            title = detail.strMeal,
            imageUrl = detail.strMealThumb,
            instructions = detail.strInstructions,
            category = detail.strCategory,
            area = detail.strArea,
            ingredients = formatIngredients(detail)
        )
    }

    /**
     * Helper function to combine TheMealDB's 20 ingredient and measure fields
     * into a single, clean list of strings.
     */
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

    /**
     * Gets a recipe's basic details from the local database.
     */
    suspend fun getDetail(id: String): RecipeEntity? = recipeDao.getById(id)
}
