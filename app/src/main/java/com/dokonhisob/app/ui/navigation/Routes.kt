package com.dokonhisob.app.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val SALES = "sales"
    const val PRODUCTS = "products"
    const val DEBTS = "debts"
    const val DEBT_DETAIL = "debts/{customerId}"
    const val EXPENSES = "expenses"
    const val REPORTS = "reports"
    const val ASSISTANT = "assistant"
    const val SETTINGS = "settings"

    fun debtDetail(customerId: Long) = "debts/$customerId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
