package com.mzansi.recipes.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface TastyService {
    @GET("featured-section")
    suspend fun listRecipes(
        @Query("from") from: Int = 0,
        @Query("size") size: Int = 20,
    ): FeaturedSectionResponse

    @GET("recipes/detail")
    suspend fun getRecipeDetail(
        @Query("id") id: String
    ): TastyRecipeDetail
}