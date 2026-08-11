package com.dokonhisob.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dokonhisob.app.data.local.dao.CustomerDao
import com.dokonhisob.app.data.local.dao.DebtPaymentDao
import com.dokonhisob.app.data.local.dao.ExpenseDao
import com.dokonhisob.app.data.local.dao.ProductDao
import com.dokonhisob.app.data.local.dao.SaleDao
import com.dokonhisob.app.data.local.entity.Customer
import com.dokonhisob.app.data.local.entity.DebtPayment
import com.dokonhisob.app.data.local.entity.Expense
import com.dokonhisob.app.data.local.entity.Product
import com.dokonhisob.app.data.local.entity.Sale
import com.dokonhisob.app.data.local.entity.SaleItem

@Database(
    entities = [Product::class, Sale::class, SaleItem::class, Expense::class, Customer::class, DebtPayment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun customerDao(): CustomerDao
    abstract fun debtPaymentDao(): DebtPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dokon_hisob_kitob.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
