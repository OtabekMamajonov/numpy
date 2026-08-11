package com.dokonhisob.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dokonhisob.app.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE createdAt >= :from AND createdAt <= :to ORDER BY createdAt DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE createdAt >= :from AND createdAt <= :to")
    suspend fun sumBetween(from: Long, to: Long): Double

    @Query(
        """
        SELECT category, SUM(amount) as total FROM expenses
        WHERE createdAt >= :from AND createdAt <= :to
        GROUP BY category ORDER BY total DESC
        """
    )
    suspend fun sumByCategory(from: Long, to: Long): List<CategoryTotal>
}

data class CategoryTotal(val category: String, val total: Double)
