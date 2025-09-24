package com.mzansi.recipes.ui.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mzansi.recipes.R
import com.mzansi.recipes.ViewModel.ShoppingViewModel
import com.mzansi.recipes.ViewModel.ShoppingViewModelFactory
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(nav: NavController) {
    val db = AppModules.provideDb(nav.context)
    val repo = AppModules.provideShoppingRepo(db, AppModules.provideFirestore(), AppModules.provideAuth())
    val vm: ShoppingViewModel = viewModel(factory = ShoppingViewModelFactory(repo))
    val items by vm.items.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.shopping_list_title),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_desc),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                windowInsets = WindowInsets.safeDrawing
            )
        },
        bottomBar = { MzansiBottomNavigationBar(navController = nav) }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                // horizontalArrangement = Arrangement.SpaceBetween, // Removed as only one item remains
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(id = R.string.shopping_list_section_header), style = MaterialTheme.typography.titleLarge)
                // Edit Button Removed
            }
            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(stringResource(id = R.string.shopping_list_empty_message), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    item.itemName, // Assuming itemName is not for localization, as it's user-generated data
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { vm.toggle(item) }
                                    )
                                    IconButton(onClick = { vm.delete(item) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(id = R.string.delete_item_action_desc),
                                            tint = Color.Red
                                        )
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
