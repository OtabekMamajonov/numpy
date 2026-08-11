package com.dokonhisob.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val purchasePrice: Double,
    val sellPrice: Double,
    val quantity: Double,
    val unit: String,
    val minQuantity: Double,
    val createdAt: Long = System.currentTimeMillis()
)
