package com.mzansi.recipes.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.HomeViewModel
import com.mzansi.recipes.ViewModel.HomeViewModelFactory
import com.mzansi.recipes.data.db.RecipeEntity
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val db = AppModules.provideDb(nav.context)
    val service = AppModules.provideTastyService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY))
    val repo = RecipeRepository(service, db.recipeDao())
    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(repo))
    val state by vm.state.collectAsState()
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        bottomBar = {
            MzansiBottomNavigationBar(navController = nav)
        },
        containerColor = MaterialTheme.colorScheme.background // sets the base background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // only respect bottom padding for nav bar
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
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search Icon", tint = Color.Gray)
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

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Categories Section
                Text("Categories", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(onClick = { /* TODO */ }, label = { Text("Breakfast") })
                    AssistChip(onClick = { /* TODO */ }, label = { Text("Lunch") })
                    AssistChip(onClick = { /* TODO */ }, label = { Text("Dinner") })
                }
                Spacer(Modifier.height(24.dp))

                // Trending Section
                Text("Trending", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                if (state.loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else if (state.error != null) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(state.trending) { recipeEntity ->
                            RecipeItem(recipe = recipeEntity, navController = nav)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                // For You Section
                Text("For You", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.trending) { recipeEntity ->
                        RecipeItem(recipe = recipeEntity, navController = nav)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: RecipeEntity, navController: NavController) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { navController.navigate(Routes.RecipeDetail.replace("{id}", recipe.id)) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(model = recipe.imageUrl),
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