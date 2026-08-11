package com.dokonhisob.app.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokonhisob.app.data.local.entity.Customer
import com.dokonhisob.app.data.local.entity.DebtPayment
import com.dokonhisob.app.data.repository.CustomerDebt
import com.dokonhisob.app.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DebtListViewModel(private val repository: ShopRepository) : ViewModel() {

    private val _debts = MutableStateFlow<List<CustomerDebt>>(emptyList())
    val debts: StateFlow<List<CustomerDebt>> = _debts.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.observeCustomers().collectLatest { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _debts.value = repository.getAllCustomerDebts().sortedByDescending { it.remaining }
        }
    }

    fun addCustomer(name: String, phone: String?) {
        viewModelScope.launch {
            repository.saveCustomer(Customer(name = name, phone = phone))
            refresh()
        }
    }
}

class DebtDetailViewModel(
    private val repository: ShopRepository,
    private val customerId: Long
) : ViewModel() {

    private val _debt = MutableStateFlow<CustomerDebt?>(null)
    val debt: StateFlow<CustomerDebt?> = _debt.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _debt.value = repository.getCustomerDebt(customerId)
        }
    }

    fun addPayment(amount: Double, note: String?) {
        viewModelScope.launch {
            repository.addDebtPayment(DebtPayment(customerId = customerId, amount = amount, note = note))
            refresh()
        }
    }
}
