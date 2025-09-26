package com.mzansi.recipes.data.api

import retrofit2.http.GET
import retrofit2.http.Query

// Basic data classes for TheMealDB response
data class MealDbResponse(val meals: List<MealSummary>)
data class MealDetailResponse(val meals: List<MealDetail>?) // meals is now nullable

// Represents a meal in a list
data class MealSummary(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String
)

// Represents the full details of a single meal
data class MealDetail(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String?,
    val strInstructions: String?,
    val strCategory: String?,
    val strArea: String?,
    val strIngredient1: String?, val strIngredient2: String?, val strIngredient3: String?, val strIngredient4: String?, val strIngredient5: String?, val strIngredient6: String?, val strIngredient7: String?, val strIngredient8: String?, val strIngredient9: String?, val strIngredient10: String?,
    val strIngredient11: String?, val strIngredient12: String?, val strIngredient13: String?, val strIngredient14: String?, val strIngredient15: String?, val strIngredient16: String?, val strIngredient17: String?, val strIngredient18: String?, val strIngredient19: String?, val strIngredient20: String?,
    val strMeasure1: String?, val strMeasure2: String?, val strMeasure3: String?, val strMeasure4: String?, val strMeasure5: String?, val strMeasure6: String?, val strMeasure7: String?, val strMeasure8: String?, val strMeasure9: String?, val strMeasure10: String?,
    val strMeasure11: String?, val strMeasure12: String?, val strMeasure13: String?, val strMeasure14: String?, val strMeasure15: String?, val strMeasure16: String?, val strMeasure17: String?, val strMeasure18: String?, val strMeasure19: String?, val strMeasure20: String?
)

// Represents a category from TheMealDB API
data class CategorySummary(
    val idCategory: String, // Though not used now, it's good to have if the API provides it
    val strCategory: String,
    val strCategoryThumb: String? = null,      // Optional image
    val strCategoryDescription: String? = null // Optional description
)

// Response for the list of categories
data class CategoriesResponse(val categories: List<CategorySummary>?)

interface MealDbService {
    @GET("latest.php")
    suspend fun getLatest(): MealDbResponse

    @GET("filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealDbResponse

    // Endpoint to filter by category
    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealDbResponse

    @GET("lookup.php")
    suspend fun lookupRecipeById(@Query("i") id: String): MealDetailResponse

    @GET("categories.php")
    suspend fun listCategories(): CategoriesResponse

    // Endpoint for searching meals by name
    @GET("search.php")
    suspend fun searchByName(@Query("s") searchTerm: String): MealDbResponse
}
