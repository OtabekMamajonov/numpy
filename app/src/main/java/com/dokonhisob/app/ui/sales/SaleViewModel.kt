package com.dokonhisob.app.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokonhisob.app.data.local.entity.Customer
import com.dokonhisob.app.data.local.entity.Product
import com.dokonhisob.app.data.repository.CartLine
import com.dokonhisob.app.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SaleUiState(
    val cart: List<CartLine> = emptyList(),
    val lastCheckoutMessage: String? = null
) {
    val total: Double get() = cart.sumOf { it.product.sellPrice * it.quantity }
}

class SaleViewModel(private val repository: ShopRepository) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(SaleUiState())
    val uiState: StateFlow<SaleUiState> = _uiState.asStateFlow()

    fun addToCart(product: Product) {
        val cart = _uiState.value.cart.toMutableList()
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = cart[index]
            cart[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cart.add(CartLine(product, 1.0))
        }
        _uiState.value = _uiState.value.copy(cart = cart)
    }

    fun updateQuantity(product: Product, quantity: Double) {
        val cart = _uiState.value.cart.toMutableList()
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            if (quantity <= 0) {
                cart.removeAt(index)
            } else {
                cart[index] = cart[index].copy(quantity = quantity)
            }
            _uiState.value = _uiState.value.copy(cart = cart)
        }
    }

    fun removeFromCart(product: Product) {
        val cart = _uiState.value.cart.filterNot { it.product.id == product.id }
        _uiState.value = _uiState.value.copy(cart = cart)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cart = emptyList())
    }

    fun checkout(customer: Customer?, isDebt: Boolean, note: String?, onDone: () -> Unit) {
        val cart = _uiState.value.cart
        if (cart.isEmpty()) return
        viewModelScope.launch {
            repository.checkout(cart, customer, isDebt, note)
            _uiState.value = SaleUiState(lastCheckoutMessage = "Sotuv muvaffaqiyatli qayd etildi")
            onDone()
        }
    }

    fun saveNewCustomer(name: String, phone: String?, onSaved: (Customer) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveCustomer(Customer(name = name, phone = phone))
            onSaved(Customer(id = id, name = name, phone = phone))
        }
    }
}
