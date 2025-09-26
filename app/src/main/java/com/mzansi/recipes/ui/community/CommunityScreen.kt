package com.mzansi.recipes.ui.community

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mzansi.recipes.data.db.CategoryEntity
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(nav: NavController) {
    val context = nav.context // Get context once
    val communityRepo = AppModules.provideCommunityRepo(
        AppModules.provideFirestore(),
        AppModules.provideAuth(),
        AppModules.provideStorage()
    )
    val db = AppModules.provideDb(context)
    val service = AppModules.provideMealDbService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY))
    // Use the singleton NetworkMonitor from AppModules
    val networkMonitor = remember { AppModules.provideNetworkMonitor(context) }
    val recipeRepo = remember {
        AppModules.provideRecipeRepo(
            service = service,
            recipeDao = db.recipeDao(),
            categoryDao = db.categoryDao(),
            networkMonitor = networkMonitor
        )
    }

    val vm: CommunityViewModel = viewModel(factory = CommunityViewModelFactory(communityRepo, recipeRepo))
    val state by vm.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadContent() }

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
                categories = state.categories, // Pass categories here
                onDismiss = { showCreateDialog = false },
                onCreate = { title, uri, ingredients, instructions, categoryName -> // Added categoryName
                    vm.create(title, uri, ingredients, instructions, categoryName) // Pass categoryName to ViewModel
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

            Text(stringResource(id = R.string.categories), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (state.isLoadingCategoriesRefresh) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else if (state.categories.isEmpty()) {
                Text(stringResource(id = R.string.no_categories_found))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories) { category ->
                        val isSelected = state.selectedCategoryName == category.name
                        AssistChip(
                            onClick = { vm.onCategorySelected(category.name) },
                            label = { Text(category.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            val titleText = if (state.selectedCategoryName != null) {
                stringResource(id = R.string.posts_in_category_title, state.selectedCategoryName!!)
            } else {
                stringResource(id = R.string.popular)
            }
            Text(titleText, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            when {
                state.loadingPosts && state.filteredPosts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                state.filteredPosts.isEmpty() -> {
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
                        items(state.filteredPosts, key = { it.postId }) { post -> // Iterate over filteredPosts
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { // ***** Updated Clickable Modifier Logic *****
                                        if (post.sourceApiId != null) {
                                            // Corrected: Use 'id' as the parameter name to match RecipeDetailScreen and Routes
                                            nav.navigate(Routes.recipeDetail(id = post.sourceApiId!!, title = post.title))
                                        } else {
                                            // Navigate to UserPostDetailScreen for user-uploaded posts
                                            nav.navigate(Routes.userPostDetail(postId = post.postId))
                                        }
                                    }
                            ) {
                                Column {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = post.imageUrl),
                                        contentDescription = post.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                    )
                                    Column(Modifier.padding(8.dp)) {
                                        Text(text = post.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = post.category ?: "Uncategorized", style = MaterialTheme.typography.bodySmall, color = Color.Gray) // Display category
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onCreate: (title: String, imageUri: Uri?, ingredients: String, instructions: String, categoryName: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var ingredientsText by remember { mutableStateOf("") }
    var instructionsText by remember { mutableStateOf("") } // Corrected mutableStateOf
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.create_post_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(id = R.string.post_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    label = { Text(stringResource(id = R.string.post_ingredients_label)) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    label = { Text(stringResource(id = R.string.post_instructions_label)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 10
                )
                Spacer(Modifier.height(16.dp))

                // Category Dropdown
                if (categories.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: stringResource(id = R.string.select_category_label),
                            onValueChange = {}, // Not directly editable
                            readOnly = true,
                            label = { Text(stringResource(id = R.string.category_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Button(onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text(stringResource(id = R.string.select_image_button))
                }
                Spacer(Modifier.height(8.dp))
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = it),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, imageUri, ingredientsText, instructionsText, selectedCategory?.name)
                    }
                },
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
