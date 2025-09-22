package com.mzansi.recipes.data.api

import com.squareup.moshi.Json

// New model for the /featured-section endpoint
data class FeaturedSectionResponse(
    @Json(name = "data") val data: FeaturedData?
)

data class FeaturedData(
    @Json(name = "sections") val sections: List<FeaturedSection> = emptyList()
)

data class FeaturedSection(
    @Json(name = "items") val items: FeaturedSectionItems?
)

data class FeaturedSectionItems(
    @Json(name = "buzzes") val buzzes: List<BuzzItem> = emptyList(),
    @Json(name = "tasty-search") val tastySearch: List<TastySearchItem> = emptyList()
)

data class BuzzItem(
    @Json(name = "_content_id") val id: String?,
    @Json(name = "title") val name: String?,
    @Json(name = "thumbnail_url") val thumbnailUrl: String?
)

data class TastySearchItem(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "thumbnail_url") val thumbnailUrl: String?
)

// Existing model for recipe details - can be kept for the detail screen
data class TastyRecipeDetail(
    val id: String?,
    val name: String?,
    @Json(name = "thumbnail_url") val thumbnailUrl: String?,
    val cook_time_minutes: Int?,
    val num_servings: Int?,
    val sections: List<Section>?
) {
    data class Section(val components: List<Component>?)
    data class Component(
        val raw_text: String?
    )
}

// Old response types - can be removed or kept for reference, but are no longer used by trending.
data class TastyListResponse(
    @Json(name = "results") val results: List<TastyRecipeBrief> = emptyList()
)

data class TastyRecipeBrief(
    val id: String?,
    val name: String?,
    @Json(name = "thumbnail_url") val thumbnailUrl: String?
)
