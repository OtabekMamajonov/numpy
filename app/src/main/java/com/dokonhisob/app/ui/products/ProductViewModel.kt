package com.dokonhisob.app.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokonhisob.app.data.local.entity.Product
import com.dokonhisob.app.data.repository.ShopRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ShopRepository) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(product: Product) {
        viewModelScope.launch { repository.saveProduct(product) }
    }

    fun delete(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }
}
