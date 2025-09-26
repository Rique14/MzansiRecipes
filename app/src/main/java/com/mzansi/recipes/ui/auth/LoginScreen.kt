package com.mzansi.recipes.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.mzansi.recipes.R
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ViewModel.AuthViewModel
import com.mzansi.recipes.ViewModel.AuthViewModelFactory
import com.mzansi.recipes.di.AppModules

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavController) {
    val repo = AppModules.provideAuthRepo(AppModules.provideAuth(), AppModules.provideFirestore())
    val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(repo))
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Configure Google Sign-In
    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Crucial for Firebase
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                    if (account != null && account.idToken != null) {
                        vm.signInWithGoogle(account.idToken!!)
                    } else {
                        Log.w("LoginScreen", "Google Sign In failed: No ID token or account found.")

                    }
                } catch (e: ApiException) {
                    Log.w("LoginScreen", "Google Sign In failed with ApiException: ${e.statusCode}", e)

                }
            } else {
                 Log.w("LoginScreen", "Google Sign In activity was cancelled or failed. Result code: ${result.resultCode}")

            }
        }
    )

    if (state.loggedIn) {
        LaunchedEffect(Unit) { nav.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } } }
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WELCOME",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email") },
                trailingIcon = { Icon(imageVector = Icons.Outlined.MailOutline, contentDescription = "Email Icon") },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password") },
                trailingIcon = { Icon(imageVector = Icons.Filled.Visibility, contentDescription = "Password Icon") },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0)
                ),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            TextButton(
                onClick = { nav.navigate(Routes.Forgot) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot password?", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.login(email.trim(), password) },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("LOGIN", color = Color.White, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don\'t have an Account? ")
                TextButton(onClick = { nav.navigate(Routes.Register) }) {
                    Text("Register", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OR", modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Logo", 
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("LOGIN IN WITH GOOGLE", color = MaterialTheme.colorScheme.onSurface)
            }

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}
