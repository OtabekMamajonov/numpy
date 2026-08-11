package com.dokonhisob.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokonhisob.app.data.local.dao.ProductSalesStat
import com.dokonhisob.app.data.local.entity.Product
import com.dokonhisob.app.data.repository.DailySummary
import com.dokonhisob.app.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DashboardUiState(
    val today: DailySummary = DailySummary(0.0, 0.0, 0.0),
    val week: DailySummary = DailySummary(0.0, 0.0, 0.0),
    val lowStock: List<Product> = emptyList(),
    val topProducts: List<ProductSalesStat> = emptyList(),
    val totalDebt: Double = 0.0,
    val isLoading: Boolean = true
)

class DashboardViewModel(private val repository: ShopRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
        viewModelScope.launch {
            repository.observeLowStock().collectLatest { list ->
                _uiState.value = _uiState.value.copy(lowStock = list)
            }
        }
    }

    fun loadSummary() {
        viewModelScope.launch {
            val todayFrom = ShopRepository.startOfDay()
            val todayTo = ShopRepository.endOfDay()
            val weekFrom = ShopRepository.daysAgo(7)

            val today = repository.getSummary(todayFrom, todayTo)
            val week = repository.getSummary(weekFrom, todayTo)
            val topProducts = repository.getTopProducts(weekFrom, todayTo, 5)
            val debts = repository.getAllCustomerDebts().sumOf { it.remaining }

            _uiState.value = _uiState.value.copy(
                today = today,
                week = week,
                topProducts = topProducts,
                totalDebt = debts,
                isLoading = false
            )
        }
    }
}
