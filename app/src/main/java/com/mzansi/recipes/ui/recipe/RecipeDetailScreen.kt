package com.mzansi.recipes.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark // <<< IMPORT
import androidx.compose.material.icons.outlined.BookmarkBorder // <<< IMPORT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.RecipeDetailViewModel
import com.mzansi.recipes.ViewModel.RecipeDetailViewModelFactory
// import com.mzansi.recipes.data.repo.RecipeRepository // No longer directly needed here for instantiation
import com.mzansi.recipes.di.AppModules


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(nav: NavController, id: String, title: String) {
    val context = LocalContext.current
    val db = AppModules.provideDb(context)
    val shoppingRepo = AppModules.provideShoppingRepo(db, AppModules.provideFirestore(), AppModules.provideAuth())
    val service = AppModules.provideMealDbService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY))

    val networkMonitor = remember { AppModules.provideNetworkMonitor(context) }

    // Updated RecipeRepository instantiation to use AppModules for all dependencies
    val recipeRepo = remember {
        AppModules.provideRecipeRepo(
            service = service,
            recipeDao = db.recipeDao(),
            categoryDao = db.categoryDao(),
            networkMonitor = networkMonitor
        )
    }
    val detailVm: RecipeDetailViewModel = viewModel(factory = RecipeDetailViewModelFactory(recipeRepo, shoppingRepo, id))
    val state by detailVm.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.recipe_detail), color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontSize = 30.sp) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { detailVm.toggleSaveRecipe() }) {
                        Icon(
                            imageVector = if (state.isSavedOffline) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (state.isSavedOffline) "Unsave Recipe" else "Save Recipe",
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
        when {
            state.loading && state.details == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.details == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
            }
            state.details != null -> {
                val details = state.details!!
                Column(
                    Modifier
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = details.imageUrl),
                        contentDescription = details.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(details.title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        val areaDisplay = details.area?.takeIf { it.isNotBlank() && it.equals("null", ignoreCase = true).not() } ?: "N/A"
                        val categoryDisplay = details.category?.takeIf { it.isNotBlank() && it.equals("null", ignoreCase = true).not() } ?: stringResource(id = R.string.uncategorized_label)
                        Text("$areaDisplay | $categoryDisplay", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(id = R.string.ingredients), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))

                        // Ingredients in two columns
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                details.ingredients.forEachIndexed { index, item ->
                                    if (index % 2 == 0) {
                                        IngredientRow(
                                            item = item,
                                            checked = state.ingredientSelection[item] ?: false,
                                            onCheckedChange = { detailVm.toggleIngredientSelection(item) }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(16.dp)) // Space between columns
                            Column(Modifier.weight(1f)) {
                                details.ingredients.forEachIndexed { index, item ->
                                    if (index % 2 != 0) {
                                        IngredientRow(
                                            item = item,
                                            checked = state.ingredientSelection[item] ?: false,
                                            onCheckedChange = { detailVm.toggleIngredientSelection(item) }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(id = R.string.instructions), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        val instructionsList = details.instructions?.lines()?.filter { it.isNotBlank() } ?: emptyList()
                        instructionsList.forEachIndexed { index, instruction ->
                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                Text("${index + 1}. ", fontWeight = FontWeight.Bold)
                                Text(instruction)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { detailVm.addAllIngredientsToShopping() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                                .height(50.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(id = R.string.add), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            // Fallback for when details are null but not loading and no error specific to details loading
            else -> {
                 Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Recipe details not available.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun IngredientRow(item: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked, 
            onCheckedChange = { newCheckedState -> 
                onCheckedChange(newCheckedState) 
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(item, style = MaterialTheme.typography.bodyMedium)
    }
}
