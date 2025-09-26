package com.mzansi.recipes.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.DisplayMode
import com.mzansi.recipes.ViewModel.RecipeViewModel
import com.mzansi.recipes.ViewModel.RecipeViewModelFactory
import com.mzansi.recipes.data.db.RecipeEntity
// import com.mzansi.recipes.data.repo.RecipeRepository // No longer directly needed here for instantiation
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar
// import com.mzansi.recipes.util.NetworkMonitor // No longer directly needed here for instantiation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val context = nav.context
    val db = AppModules.provideDb(context)
    val service = AppModules.provideMealDbService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY))
    // Use the singleton NetworkMonitor from AppModules
    val networkMonitor = remember { AppModules.provideNetworkMonitor(context) } 
    
    // Updated RecipeRepository instantiation to use AppModules for all dependencies
    val repo = remember {
        AppModules.provideRecipeRepo(
            service = service,
            recipeDao = db.recipeDao(),
            categoryDao = db.categoryDao(),
            networkMonitor = networkMonitor
        )
    }
    val vm: RecipeViewModel = viewModel(factory = RecipeViewModelFactory(repo))
    val state by vm.state.collectAsState()

    Scaffold(
        bottomBar = {
            MzansiBottomNavigationBar(navController = nav)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pot_icon),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    TextField(
                        value = state.searchQuery,
                        onValueChange = { vm.onSearchQueryChanged(it) },
                        placeholder = { Text(stringResource(id = R.string.search), color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.search), tint = Color.Gray)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // Main Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))

                // Categories Section
                Text(stringResource(id = R.string.categories), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                // Updated loading state and category property access
                if (state.isLoadingCategoriesRefresh) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else if (state.categories.isEmpty()){
                    Text(stringResource(id = R.string.no_categories_found))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.categories) { category ->
                            AssistChip(
                                onClick = { vm.loadRecipesForCategory(category.name) },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                // Trending Section
                Text(stringResource(id = R.string.trending), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                // Updated loading state
                if (state.isLoadingTrendingRefresh) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else if (state.trendingRecipes.isEmpty()) {
                    Text(stringResource(id = R.string.no_trending_recipes))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(state.trendingRecipes) { recipeEntity ->
                            RecipeItem(recipe = recipeEntity, navController = nav)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                // Dynamic Content Area (Search Results / Category Recipes / Placeholder)
                when (state.activeDisplayMode) {
                    DisplayMode.SEARCH_RESULTS -> {
                        Text(
                            stringResource(id = R.string.search_results_title, state.searchQuery),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        if (state.isLoadingSearchResults) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        } else if (state.searchResults.isEmpty()) {
                            Text(stringResource(id = R.string.no_search_results, state.searchQuery))
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(state.searchResults) { recipeEntity ->
                                    RecipeItem(recipe = recipeEntity, navController = nav)
                                }
                            }
                        }
                    }
                    DisplayMode.CATEGORY_RECIPES -> {
                        Text(
                            state.selectedCategoryName?.let {
                                stringResource(id = R.string.category_recipes_title, it)
                            } ?: stringResource(id = R.string.categories), // Fallback title
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        // Updated loading state
                        if (state.isLoadingCategoryRecipesRefresh) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        } else if (state.recipesForCategory.isEmpty()) {
                            Text(stringResource(id = R.string.no_category_recipes, state.selectedCategoryName ?: "this category"))
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(state.recipesForCategory) { recipeEntity ->
                                    RecipeItem(recipe = recipeEntity, navController = nav)
                                }
                            }
                        }
                    }
                    DisplayMode.TRENDING_ONLY -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center){
                             Text(
                                 stringResource(id = R.string.explore_recipes_placeholder),
                                 style = MaterialTheme.typography.bodyLarge,
                                 textAlign = TextAlign.Center
                             )
                        }
                    }
                }
                // Updated loading state checks for error display
                if (state.error != null && 
                    !state.isLoadingTrendingRefresh && 
                    !state.isLoadingCategoriesRefresh && 
                    !state.isLoadingSearchResults && 
                    !state.isLoadingCategoryRecipesRefresh) {
                     Text(
                         stringResource(id = R.string.error_loading_recipes) + "\n${state.error}", 
                         color = MaterialTheme.colorScheme.error, 
                         modifier = Modifier.padding(top = 8.dp)
                     )
                }
                Spacer(Modifier.height(16.dp)) // Padding at the bottom of the scrollable content
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: RecipeEntity, navController: NavController) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { navController.navigate(Routes.recipeDetail(recipe.id, recipe.title)) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(
                    model = recipe.imageUrl,
                    error = painterResource(id = R.drawable.ic_placeholder_image), // Placeholder on error
                    placeholder = painterResource(id = R.drawable.ic_placeholder_image) // Placeholder while loading
                ),
                contentDescription = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recipe.title,
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
