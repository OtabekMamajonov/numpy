package com.dokonhisob.app.data.repository

import com.dokonhisob.app.data.local.AppDatabase
import com.dokonhisob.app.data.local.dao.CategoryTotal
import com.dokonhisob.app.data.local.dao.ProductSalesStat
import com.dokonhisob.app.data.local.entity.Customer
import com.dokonhisob.app.data.local.entity.DebtPayment
import com.dokonhisob.app.data.local.entity.Expense
import com.dokonhisob.app.data.local.entity.Product
import com.dokonhisob.app.data.local.entity.Sale
import com.dokonhisob.app.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

data class CartLine(
    val product: Product,
    val quantity: Double
)

data class CustomerDebt(
    val customer: Customer,
    val totalDebt: Double,
    val totalPaid: Double
) {
    val remaining: Double get() = totalDebt - totalPaid
}

data class DailySummary(
    val revenue: Double,
    val profit: Double,
    val expenses: Double
) {
    val netProfit: Double get() = profit - expenses
}

class ShopRepository(private val db: AppDatabase) {

    // ---------- Products ----------
    fun observeProducts(): Flow<List<Product>> = db.productDao().observeAll()
    fun observeLowStock(): Flow<List<Product>> = db.productDao().observeLowStock()
    suspend fun saveProduct(product: Product) = db.productDao().upsert(product)
    suspend fun deleteProduct(product: Product) = db.productDao().delete(product)

    // ---------- Sales ----------
    fun observeSales(): Flow<List<Sale>> = db.saleDao().observeAll()

    suspend fun checkout(cart: List<CartLine>, customer: Customer?, isDebt: Boolean, note: String?): Long {
        val items = cart.map { line ->
            SaleItem(
                saleId = 0,
                productId = line.product.id,
                productName = line.product.name,
                quantity = line.quantity,
                unitPrice = line.product.sellPrice,
                unitPurchasePrice = line.product.purchasePrice,
                totalPrice = line.product.sellPrice * line.quantity
            )
        }
        val totalAmount = items.sumOf { it.totalPrice }
        val totalProfit = items.sumOf { (it.unitPrice - it.unitPurchasePrice) * it.quantity }

        val sale = Sale(
            totalAmount = totalAmount,
            totalProfit = totalProfit,
            customerId = customer?.id,
            isDebt = isDebt,
            note = note
        )

        val saleId = db.saleDao().insertSaleWithItems(sale, items)

        cart.forEach { line ->
            db.productDao().decreaseStock(line.product.id, line.quantity)
        }

        return saleId
    }

    // ---------- Expenses ----------
    fun observeExpenses(): Flow<List<Expense>> = db.expenseDao().observeAll()
    suspend fun addExpense(expense: Expense) = db.expenseDao().insert(expense)
    suspend fun deleteExpense(expense: Expense) = db.expenseDao().delete(expense)

    // ---------- Customers / Debts ----------
    fun observeCustomers(): Flow<List<Customer>> = db.customerDao().observeAll()
    suspend fun saveCustomer(customer: Customer) = db.customerDao().upsert(customer)

    suspend fun getCustomerDebt(customerId: Long): CustomerDebt? {
        val customer = db.customerDao().getById(customerId) ?: return null
        val debt = db.saleDao().sumDebtForCustomer(customerId)
        val paid = db.debtPaymentDao().sumForCustomer(customerId)
        return CustomerDebt(customer, debt, paid)
    }

    suspend fun addDebtPayment(payment: DebtPayment) = db.debtPaymentDao().insert(payment)

    suspend fun getAllCustomerDebts(): List<CustomerDebt> {
        val customers = db.customerDao().observeAll().first()
        return customers.mapNotNull { getCustomerDebt(it.id) }
    }

    // ---------- Reports ----------
    suspend fun getSummary(from: Long, to: Long): DailySummary {
        val revenue = db.saleDao().sumAmountBetween(from, to)
        val profit = db.saleDao().sumProfitBetween(from, to)
        val expenses = db.expenseDao().sumBetween(from, to)
        return DailySummary(revenue, profit, expenses)
    }

    suspend fun getTopProducts(from: Long, to: Long, limit: Int = 5): List<ProductSalesStat> =
        db.saleDao().topProducts(from, to, limit)

    suspend fun getExpensesByCategory(from: Long, to: Long): List<CategoryTotal> =
        db.expenseDao().sumByCategory(from, to)

    companion object {
        fun startOfDay(timestamp: Long = System.currentTimeMillis()): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun endOfDay(timestamp: Long = System.currentTimeMillis()): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.timeInMillis
        }

        fun daysAgo(days: Int): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -days)
            return startOfDay(cal.timeInMillis)
        }
    }
}
