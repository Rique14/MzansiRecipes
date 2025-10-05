package com.mzansi.recipes.ui.userpost

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.UserPostDetailViewModel
import com.mzansi.recipes.ViewModel.UserPostDetailViewModelFactory
import com.mzansi.recipes.di.AppModules
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPostDetailScreen(
    navController: NavController,
    postId: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val db = AppModules.provideDb(context)
    val service = AppModules.provideMealDbService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY))
    val networkMonitor = remember { AppModules.provideNetworkMonitor(context) }

    // Correctly instantiate all repositories using positional arguments
    val communityRepository = remember {
        AppModules.provideCommunityRepo(
            AppModules.provideFirestore(),
            AppModules.provideAuth(),
            AppModules.provideStorage()
        )
    }
    val shoppingRepository = remember {
        AppModules.provideShoppingRepo(
            db,
            AppModules.provideFirestore(),
            AppModules.provideAuth()
        )
    }
    val recipeRepository = remember {
        AppModules.provideRecipeRepo(
            service,
            db.recipeDao(),
            db.categoryDao(),
            networkMonitor
        )
    }

    // Instantiate ViewModel using the updated factory with all three repositories
    val viewModel: UserPostDetailViewModel = viewModel(
        factory = UserPostDetailViewModelFactory(communityRepository, shoppingRepository, recipeRepository),
        key = postId
    )

    val uiState by viewModel.uiState.collectAsState()
    val post = uiState.post

    // Effect to show Snackbar when items are added
    LaunchedEffect(uiState.itemsAddedToCart) {
        if (uiState.itemsAddedToCart) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.ingredients_added_to_shopping_list),
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.onAddedToCartHandled() // Reset the event
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.recipe_detail), color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontSize = 30.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_desc), tint = Color.White)
                    }
                },
                actions = {
                    // Save recipe button
                    IconButton(onClick = { viewModel.toggleSaveRecipe() }) {
                        Icon(
                            imageVector = if (uiState.isSavedOffline) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (uiState.isSavedOffline) "Unsave Recipe" else "Save Recipe",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (uiState.ingredients.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.addSelectedIngredientsToCart() },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                    text = { Text(stringResource(R.string.add_to_list)) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
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
                post != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // Space for the FAB
                    ) {
                        item {
                            if (!post.imageUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = post.imageUrl,
                                        error = painterResource(id = R.drawable.ic_placeholder_image),
                                        placeholder = painterResource(id = R.drawable.ic_placeholder_image)
                                    ),
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Header Section
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(text = post.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = post.category ?: stringResource(id = R.string.uncategorized_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.likePost() }) {
                                        Icon(
                                            Icons.Filled.Favorite,
                                            contentDescription = stringResource(id = R.string.like_post_desc),
                                            tint = if (post.likes > 0) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    }
                                    Text("${post.likes}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Ingredients Section
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = stringResource(id = R.string.ingredients),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (uiState.ingredients.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.no_ingredients_provided),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            items(uiState.ingredients) { ingredient ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleIngredientSelection(ingredient.name) }
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = ingredient.isSelected,
                                        onCheckedChange = { viewModel.toggleIngredientSelection(ingredient.name) }
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        // Instructions Section
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(id = R.string.instructions),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = post.instructions.ifBlank { stringResource(id = R.string.no_instructions_provided) },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(id = R.string.post_not_found_error),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
