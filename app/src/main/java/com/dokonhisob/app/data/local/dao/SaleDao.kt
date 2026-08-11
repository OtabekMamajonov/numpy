package com.dokonhisob.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.dokonhisob.app.data.local.entity.Sale
import com.dokonhisob.app.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow

data class ProductSalesStat(
    val productName: String,
    val totalQuantity: Double,
    val totalRevenue: Double
)

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Transaction
    suspend fun insertSaleWithItems(sale: Sale, items: List<SaleItem>): Long {
        val saleId = insertSale(sale)
        insertSaleItems(items.map { it.copy(saleId = saleId) })
        return saleId
    }

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE createdAt >= :from AND createdAt <= :to ORDER BY createdAt DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItem>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM sales WHERE createdAt >= :from AND createdAt <= :to")
    suspend fun sumAmountBetween(from: Long, to: Long): Double

    @Query("SELECT COALESCE(SUM(totalProfit), 0) FROM sales WHERE createdAt >= :from AND createdAt <= :to")
    suspend fun sumProfitBetween(from: Long, to: Long): Double

    @Query(
        """
        SELECT productName, SUM(quantity) as totalQuantity, SUM(totalPrice) as totalRevenue
        FROM sale_items
        INNER JOIN sales ON sale_items.saleId = sales.id
        WHERE sales.createdAt >= :from AND sales.createdAt <= :to
        GROUP BY productName
        ORDER BY totalRevenue DESC
        LIMIT :limit
        """
    )
    suspend fun topProducts(from: Long, to: Long, limit: Int = 5): List<ProductSalesStat>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM sales WHERE isDebt = 1 AND customerId = :customerId")
    suspend fun sumDebtForCustomer(customerId: Long): Double

    @Query("SELECT * FROM sales WHERE isDebt = 1 AND customerId = :customerId ORDER BY createdAt DESC")
    fun observeDebtSalesForCustomer(customerId: Long): Flow<List<Sale>>
}
