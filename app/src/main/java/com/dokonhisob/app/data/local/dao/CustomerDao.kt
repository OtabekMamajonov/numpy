package com.dokonhisob.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dokonhisob.app.data.local.entity.Customer
import com.dokonhisob.app.data.local.entity.DebtPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: Customer): Long

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun observeAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?
}

@Dao
interface DebtPaymentDao {
    @Insert
    suspend fun insert(payment: DebtPayment): Long

    @Query("SELECT * FROM debt_payments WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun observeForCustomer(customerId: Long): Flow<List<DebtPayment>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM debt_payments WHERE customerId = :customerId")
    suspend fun sumForCustomer(customerId: Long): Double
}
