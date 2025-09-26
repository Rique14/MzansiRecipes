package com.mzansi.recipes.ui.settings

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mzansi.recipes.R
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes // <<< ADDED IMPORT
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val currentUser = AppModules.provideAuth().currentUser
    var firestoreUserName by remember { mutableStateOf<String?>(null) }
    var isLoadingName by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            isLoadingName = true
            try {
                val firestore = AppModules.provideFirestore()
                val userDocument = firestore.collection("users").document(currentUser.uid).get().await()
                if (userDocument.exists()) {
                    firestoreUserName = userDocument.getString("name")
                    Log.d("ProfileScreen", "Fetched name from Firestore: $firestoreUserName")
                } else {
                    Log.d("ProfileScreen", "User document does not exist in Firestore for UID: ${currentUser.uid}")
                    firestoreUserName = null
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error fetching user name from Firestore", e)
                firestoreUserName = null // Set to null on error
            }
            isLoadingName = false
        } else {
            isLoadingName = false // No user, so not loading
            firestoreUserName = null
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
            // Changed verticalArrangement to start to accommodate the new button
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (currentUser?.photoUrl != null) {
                AsyncImage(
                    model = currentUser.photoUrl.toString(),
                    contentDescription = stringResource(id = R.string.profile_picture_content_desc),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_profile_placeholder_foreground)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder_foreground),
                    contentDescription = stringResource(id = R.string.profile_picture_content_desc),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingName) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = firestoreUserName ?: currentUser?.displayName ?: stringResource(id = R.string.guest_user), // Fallback chain
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = currentUser?.email ?: stringResource(id = R.string.no_email_available), // Fallback for email
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // <<< NEW BUTTON >>>
            Button(
                onClick = { navController.navigate(Routes.SavedRecipes) },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(stringResource(id = R.string.saved_recipes_title)) // Re-using existing string
            }
        }
    }
}
