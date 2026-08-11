package com.dokonhisob.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dokonhisob.app.DokonApplication
import com.dokonhisob.app.ui.assistant.AssistantScreen
import com.dokonhisob.app.ui.assistant.AssistantViewModel
import com.dokonhisob.app.ui.dashboard.DashboardScreen
import com.dokonhisob.app.ui.dashboard.DashboardViewModel
import com.dokonhisob.app.ui.debts.DebtDetailScreen
import com.dokonhisob.app.ui.debts.DebtDetailViewModel
import com.dokonhisob.app.ui.debts.DebtListViewModel
import com.dokonhisob.app.ui.debts.DebtsScreen
import com.dokonhisob.app.ui.expenses.ExpenseViewModel
import com.dokonhisob.app.ui.expenses.ExpensesScreen
import com.dokonhisob.app.ui.products.ProductViewModel
import com.dokonhisob.app.ui.products.ProductsScreen
import com.dokonhisob.app.ui.reports.ReportViewModel
import com.dokonhisob.app.ui.reports.ReportsScreen
import com.dokonhisob.app.ui.sales.SaleScreen
import com.dokonhisob.app.ui.sales.SaleViewModel
import com.dokonhisob.app.ui.settings.SettingsScreen
import com.dokonhisob.app.ui.settings.SettingsViewModel

private val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Bosh sahifa", Icons.Default.Home),
    BottomNavItem(Routes.SALES, "Sotuv", Icons.Default.ShoppingCart),
    BottomNavItem(Routes.PRODUCTS, "Ombor", Icons.Default.Inventory),
    BottomNavItem(Routes.DEBTS, "Qarzlar", Icons.Default.AccountBalanceWallet),
    BottomNavItem(Routes.ASSISTANT, "AI Yordamchi", Icons.Default.Chat)
)

@Composable
fun DokonNavGraph(app: DokonApplication) {
    val navController = rememberNavController()
    val currency by app.settingsManager.currencyFlow.collectAsState()
    val shopName by app.settingsManager.shopNameFlow.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                            label = { androidx.compose.material3.Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { DashboardViewModel(app.shopRepository) } }
                )
                DashboardScreen(
                    viewModel = vm,
                    shopName = shopName,
                    currency = currency,
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenExpenses = { navController.navigate(Routes.EXPENSES) },
                    onOpenReports = { navController.navigate(Routes.REPORTS) }
                )
            }

            composable(Routes.SALES) {
                val vm: SaleViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { SaleViewModel(app.shopRepository) } }
                )
                SaleScreen(viewModel = vm, currency = currency)
            }

            composable(Routes.PRODUCTS) {
                val vm: ProductViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { ProductViewModel(app.shopRepository) } }
                )
                ProductsScreen(viewModel = vm, currency = currency)
            }

            composable(Routes.DEBTS) {
                val vm: DebtListViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { DebtListViewModel(app.shopRepository) } }
                )
                DebtsScreen(
                    viewModel = vm,
                    currency = currency,
                    onOpenDetail = { customerId -> navController.navigate(Routes.debtDetail(customerId)) }
                )
            }

            composable(
                route = Routes.DEBT_DETAIL,
                arguments = listOf(navArgument("customerId") { type = androidx.navigation.NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                val vm: DebtDetailViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { DebtDetailViewModel(app.shopRepository, customerId) } }
                )
                DebtDetailScreen(viewModel = vm, currency = currency, onBack = { navController.popBackStack() })
            }

            composable(Routes.EXPENSES) {
                val vm: ExpenseViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { ExpenseViewModel(app.shopRepository) } }
                )
                ExpensesScreen(viewModel = vm, currency = currency, onBack = { navController.popBackStack() })
            }

            composable(Routes.REPORTS) {
                val vm: ReportViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { ReportViewModel(app.shopRepository) } }
                )
                ReportsScreen(viewModel = vm, currency = currency, onBack = { navController.popBackStack() })
            }

            composable(Routes.ASSISTANT) {
                val vm: AssistantViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { AssistantViewModel(app.aiAssistantRepository) } }
                )
                AssistantScreen(viewModel = vm)
            }

            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = composeViewModel(
                    factory = viewModelFactory { initializer { SettingsViewModel(app.settingsManager) } }
                )
                SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
