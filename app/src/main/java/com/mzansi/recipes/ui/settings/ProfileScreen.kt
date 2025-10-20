package com.mzansi.recipes.ui.settings

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.AuthViewModel
import com.mzansi.recipes.ViewModel.AuthViewModelFactory
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val authRepo = remember { AppModules.provideAuthRepo(AppModules.provideAuth(), AppModules.provideFirestore()) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepo))
    val authState by authViewModel.state.collectAsState()

    val currentUser = AppModules.provideAuth().currentUser
    var firestoreUserName by remember { mutableStateOf<String?>(null) }
    var isLoadingName by remember { mutableStateOf(true) }

    // State for edit mode
    var isEditing by remember { mutableStateOf(false) }
    var editableName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

    // Fetch user's name from Firestore on launch
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            isLoadingName = true
            try {
                val userDocument = AppModules.provideFirestore().collection("users").document(currentUser.uid).get().await()
                firestoreUserName = userDocument.getString("name")
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error fetching user name", e)
                firestoreUserName = null
            }
            isLoadingName = false
        } else {
            isLoadingName = false
            firestoreUserName = null
        }
    }

    // When user data is loaded or edit mode is cancelled, reset the editable fields
    LaunchedEffect(firestoreUserName, currentUser?.displayName, isEditing) {
        if (!isEditing) {
            editableName = firestoreUserName ?: currentUser?.displayName ?: ""
            selectedImageUri = null // Clear image selection when not editing
        }
    }

    // When an update is successful, exit edit mode
    LaunchedEffect(authState.updateSuccess) {
        if (authState.updateSuccess) {
            isEditing = false
            authViewModel.onUpdateSuccessHandled() // Reset the flag in ViewModel
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.profile),
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
                            contentDescription = stringResource(id = R.string.back_button_desc),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets.safeDrawing
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            val imageModifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .let { if (isEditing) it.clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) } else it }

            AsyncImage(
                model = selectedImageUri ?: currentUser?.photoUrl?.toString() ?: R.drawable.ic_profile_placeholder_foreground,
                contentDescription = stringResource(id = R.string.profile_picture_content_desc),
                modifier = imageModifier,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_profile_placeholder_foreground),
                error = painterResource(id = R.drawable.ic_profile_placeholder_foreground)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingName && !isEditing) {
                CircularProgressIndicator()
            } else if (isEditing) {
                OutlinedTextField(
                    value = editableName,
                    onValueChange = { editableName = it },
                    label = { Text("Name") },
                    singleLine = true
                )
            } else {
                Text(
                    text = firestoreUserName ?: currentUser?.displayName ?: stringResource(id = R.string.guest_user),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = currentUser?.email ?: stringResource(id = R.string.no_email_available),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (authState.loading) {
                        CircularProgressIndicator()
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { authViewModel.updateUserProfile(editableName, selectedImageUri) },
                                enabled = editableName.isNotBlank()
                            ) { Text("Save") }
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedButton(onClick = { isEditing = false }) {
                                Text("Cancel")
                            }
                        }
                    }
                    authState.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

            } else {
                Button(
                    onClick = { navController.navigate(Routes.SavedRecipes) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(stringResource(id = R.string.saved_recipes_title))
                }
            }
        }
    }
}
