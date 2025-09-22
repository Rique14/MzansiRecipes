package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.db.ShoppingItemEntity
import com.mzansi.recipes.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(private val repo: ShoppingRepository) : ViewModel() {
    val items = repo.observe().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addItem(name: String) = viewModelScope.launch { repo.addItem(name) }
    fun toggle(item: ShoppingItemEntity) = viewModelScope.launch { repo.toggleChecked(item.id, !item.isChecked) }
    fun delete(item: ShoppingItemEntity) = viewModelScope.launch { repo.delete(item) }
}