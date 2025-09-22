package com.mzansi.recipes.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mzansi.recipes.ViewModel.AuthViewModel
import com.mzansi.recipes.ViewModel.AuthViewModelFactory
import com.mzansi.recipes.di.AppModules

@Composable
fun ForgotPasswordScreen(nav: NavController) {
    val repo = AppModules.provideAuthRepo(AppModules.provideAuth(), AppModules.provideFirestore())
    val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(repo))
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Reset Password", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.forgot(email.trim()); sent = true }) { Text("SEND RESET LINK") }
        if (sent) { Spacer(Modifier.height(8.dp)); Text("Check your email.") }
    }
}