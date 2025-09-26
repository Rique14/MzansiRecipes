package com.mzansi.recipes.ui.saved

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.SavedRecipesViewModel
import com.mzansi.recipes.ViewModel.SavedRecipesViewModelFactory
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ui.home.RecipeItem // Assuming RecipeItem is accessible

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesScreen(navController: NavController) {
    val context = LocalContext.current
    val recipeRepository = remember {
        AppModules.provideRecipeRepo(
            service = AppModules.provideMealDbService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY)),
            recipeDao = AppModules.provideDb(context).recipeDao(),
            categoryDao = AppModules.provideDb(context).categoryDao(),
            networkMonitor = AppModules.provideNetworkMonitor(context)
        )
    }
    val viewModel: SavedRecipesViewModel = viewModel(
        factory = SavedRecipesViewModelFactory(recipeRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.saved_recipes_title), // Will add this string
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.savedRecipes.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                !uiState.isLoading && uiState.savedRecipes.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.no_saved_recipes), // Will add this string
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.savedRecipes) { recipe ->
                            RecipeItem(recipe = recipe, navController = navController)
                        }
                    }
                }
            }
        }
    }
}