package com.mzansi.recipes.ui.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.CommunityViewModel
import com.mzansi.recipes.ViewModel.CommunityViewModelFactory
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(nav: NavController) {
    val communityRepo = AppModules.provideCommunityRepo(AppModules.provideFirestore(), AppModules.provideAuth())
    // Instantiate RecipeRepository, similar to HomeScreen
    val recipeRepo = RecipeRepository(
        AppModules.provideMealDbService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY)),
        AppModules.provideDb(nav.context).recipeDao()
    )
    val vm: CommunityViewModel = viewModel(factory = CommunityViewModelFactory(communityRepo, recipeRepo))
    val state by vm.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadContent() } // Updated to loadContent

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.community), color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontSize = 30.sp) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets.safeDrawing
            )
        },
        bottomBar = { MzansiBottomNavigationBar(navController = nav) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.create_post_title))
            }
        }
    ) { paddingValues ->
        if (showCreateDialog) {
            CreatePostDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title ->
                    vm.create(title)
                    showCreateDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Categories Section
            Text(stringResource(id = R.string.categories), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (state.isLoadingCategories) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else if (state.categories.isEmpty()) {
                Text(stringResource(id = R.string.no_categories_found))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories) { category ->
                        AssistChip(
                            onClick = { /* TODO: Implement category filtering for community posts if needed */ },
                            label = { Text(category.strCategory) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Posts Section title (or other relevant title like "Popular Posts")
            Text(stringResource(id = R.string.popular), style = MaterialTheme.typography.titleLarge) // Example title
            Spacer(Modifier.height(8.dp))

            when {
                state.loadingPosts -> { // Changed from state.loading
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                state.posts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(id = R.string.no_posts_message), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.posts, key = { it.postId }) { post ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = post.imageUrl),
                                        contentDescription = post.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                    )
                                    Column(Modifier.padding(8.dp)) {
                                        Text(text = post.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            IconButton(onClick = { vm.like(post.postId) }, modifier = Modifier.size(24.dp)) {
                                                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Like post", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                            Text(text = "${post.likes}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.create_post_title)) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.post_title_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onCreate(title) },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(id = R.string.post))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
