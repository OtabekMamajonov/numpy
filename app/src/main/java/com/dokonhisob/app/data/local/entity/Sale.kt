package com.dokonhisob.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalAmount: Double,
    val totalProfit: Double,
    val customerId: Long? = null,
    val isDebt: Boolean = false,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
