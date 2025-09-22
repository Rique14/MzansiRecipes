package com.mzansi.recipes.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.ViewModel.RecipeDetailViewModel
import com.mzansi.recipes.ViewModel.RecipeDetailViewModelFactory
import com.mzansi.recipes.ViewModel.ShoppingViewModel
import com.mzansi.recipes.ViewModel.ShoppingViewModelFactory
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.di.AppModules

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(nav: NavController, id: String) {
    val db = AppModules.provideDb(nav.context)
    val shoppingRepo = AppModules.provideShoppingRepo(db, AppModules.provideFirestore(), AppModules.provideAuth())
    val shoppingVm: ShoppingViewModel = viewModel(factory = ShoppingViewModelFactory(shoppingRepo))

    val recipeRepo = RecipeRepository(AppModules.provideTastyService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY)), db.recipeDao())
    val detailVm: RecipeDetailViewModel = viewModel(factory = RecipeDetailViewModelFactory(recipeRepo, id))
    val state by detailVm.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.details?.title ?: "Recipe", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontSize = 30.sp) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        when {
            state.loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ready in: ${details.prepTime ?: '?'} min", style = MaterialTheme.typography.bodyMedium)
                            Text("Serves: ${details.servings ?: '?'}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(24.dp))
                        Text("Ingredients", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))

                        // Ingredients in two columns
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                details.ingredients.forEachIndexed { index, item ->
                                    if (index % 2 == 0) {
                                        IngredientRow(item = item, onCheckedChange = { /* TODO */ })
                                    }
                                }
                            }
                            Spacer(Modifier.width(16.dp)) // Space between columns
                            Column(Modifier.weight(1f)) {
                                details.ingredients.forEachIndexed { index, item ->
                                    if (index % 2 != 0) {
                                        IngredientRow(item = item, onCheckedChange = { /* TODO */ })
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                details.ingredients.forEach { shoppingVm.addItem(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                                .height(50.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("ADD", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientRow(item: String, checked: Boolean = false, onCheckedChange: (Boolean) -> Unit) {
    var isChecked by remember { mutableStateOf(checked) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { 
                isChecked = it 
                onCheckedChange(it)
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(item, style = MaterialTheme.typography.bodyMedium)
    }
}
