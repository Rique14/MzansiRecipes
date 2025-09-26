package com.mzansi.recipes.ui.userpost

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.platform.LocalSavedStateRegistryOwner // Removed import
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.UserPostDetailViewModel
import com.mzansi.recipes.ViewModel.UserPostDetailViewModelFactory
import com.mzansi.recipes.di.AppModules

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPostDetailScreen(
    navController: NavController,
    postId: String // Though primarily used by SavedStateHandle in VM via factory
) {
    val context = LocalContext.current
    val communityRepository = AppModules.provideCommunityRepo(
        AppModules.provideFirestore(),
        AppModules.provideAuth(),
        AppModules.provideStorage()
    )

    // val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current // Removed

    val viewModel: UserPostDetailViewModel = viewModel(
        factory = UserPostDetailViewModelFactory(communityRepository), // Updated factory instantiation
        key = postId // Ensures ViewModel is re-created if postId changes
    )

    val uiState by viewModel.uiState.collectAsState()
    val post = uiState.post

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(post?.title ?: stringResource(id = R.string.loading), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_desc), tint = Color.White)
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
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

                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
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

                            Text(
                                text = stringResource(id = R.string.ingredients),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = post.ingredients.ifBlank { stringResource(id = R.string.no_ingredients_provided) },
                                style = MaterialTheme.typography.bodyLarge
                            )
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
