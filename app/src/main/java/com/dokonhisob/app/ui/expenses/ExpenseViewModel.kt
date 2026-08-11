package com.dokonhisob.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokonhisob.app.data.local.entity.Expense
import com.dokonhisob.app.data.repository.ShopRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ShopRepository) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository.observeExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(expense: Expense) {
        viewModelScope.launch { repository.addExpense(expense) }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}
