package com.mzansi.recipes.ui.community



import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.BuildConfig
import com.mzansi.recipes.ViewModel.CommunityViewModel
import com.mzansi.recipes.ViewModel.CommunityViewModelFactory
import com.mzansi.recipes.data.db.CategoryEntity
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar
import androidx.compose.foundation.lazy.items



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(nav: NavController) {
    val context = LocalContext.current

    val communityRepo = remember {
        AppModules.provideCommunityRepo(
            AppModules.provideFirestore(),
            AppModules.provideAuth(),
            AppModules.provideStorage()
        )
    }

    val recipeRepo = remember {
        val db = AppModules.provideDb(context)
        val service = AppModules.provideMealDbService(AppModules.provideOkHttp(BuildConfig.RAPIDAPI_KEY))
        val networkMonitor = AppModules.provideNetworkMonitor(context)
        AppModules.provideRecipeRepo(service, db.recipeDao(), db.categoryDao(), networkMonitor)
    }

    val vm: CommunityViewModel = viewModel(factory = CommunityViewModelFactory(communityRepo, recipeRepo))
    val state by vm.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadContent() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Community", color = Color.White, fontSize = 30.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = { MzansiBottomNavigationBar(navController = nav) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { paddingValues ->

        if (showCreateDialog) {
            CreatePostDialog(
                categories = state.categories,
                onDismiss = { showCreateDialog = false },
                onCreate = { title, uri, ingredients, instructions, categoryName ->
                    vm.create(title, uri, ingredients, instructions, categoryName)
                    showCreateDialog = false
                }
            )
        }

        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Categories", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            if (state.isLoadingCategoriesRefresh) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else if (state.categories.isEmpty()) {
                Text("No categories found")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories) { category: CategoryEntity ->
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

            val titleText = state.selectedCategoryName ?: "Popular"
            Text(titleText, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            when {
                state.loadingPosts && state.filteredPosts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error) }
                }
                state.filteredPosts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No posts available") }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredPosts, key = { it.postId }) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (post.sourceApiId != null) {
                                            nav.navigate(Routes.recipeDetail(id = post.sourceApiId!!, title = post.title))
                                        } else {
                                            nav.navigate(Routes.userPostDetail(postId = post.postId))
                                        }
                                    }
                            ) {
                                Column {
                                    Image(
                                        painter = rememberAsyncImagePainter(post.imageUrl),
                                        contentDescription = post.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                    )
                                    Column(Modifier.padding(8.dp)) {
                                        Text(post.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(post.category ?: "Uncategorized", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { vm.like(post.postId) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Filled.Favorite, contentDescription = "Like post", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                            Text("${post.likes}", style = MaterialTheme.typography.bodySmall)
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
    var instructionsText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Post") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))

                Button(onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Select Image")
                }
                imageUri?.let {
                    Image(painter = rememberAsyncImagePainter(it), contentDescription = "Selected image", modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp))
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    label = { Text("Ingredients (one per line)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select a category",
                        onValueChange = {},
                        readOnly = true,
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, imageUri, ingredientsText, instructionsText, selectedCategory?.name) },
                enabled = title.isNotBlank() && ingredientsText.isNotBlank() && instructionsText.isNotBlank() && selectedCategory != null
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
