package com.dokonhisob.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokonhisob.app.data.local.dao.CategoryTotal
import com.dokonhisob.app.data.local.dao.ProductSalesStat
import com.dokonhisob.app.data.repository.DailySummary
import com.dokonhisob.app.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReportPeriod(val label: String, val days: Int) {
    WEEK("7 kun", 7),
    MONTH("30 kun", 30),
    QUARTER("90 kun", 90)
}

data class ReportUiState(
    val period: ReportPeriod = ReportPeriod.WEEK,
    val summary: DailySummary = DailySummary(0.0, 0.0, 0.0),
    val topProducts: List<ProductSalesStat> = emptyList(),
    val expensesByCategory: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = true
)

class ReportViewModel(private val repository: ShopRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        load(ReportPeriod.WEEK)
    }

    fun load(period: ReportPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(period = period, isLoading = true)
            val from = ShopRepository.daysAgo(period.days)
            val to = ShopRepository.endOfDay()
            val summary = repository.getSummary(from, to)
            val topProducts = repository.getTopProducts(from, to, 10)
            val expensesByCategory = repository.getExpensesByCategory(from, to)
            _uiState.value = _uiState.value.copy(
                summary = summary,
                topProducts = topProducts,
                expensesByCategory = expensesByCategory,
                isLoading = false
            )
        }
    }
}
