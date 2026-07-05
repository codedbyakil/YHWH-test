package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val phone: String,
    val role: String, // "Owner" or "Staff"
    val name: String,
    val businessId: Long
)

@Entity(tableName = "businesses")
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val logo: String? = null,
    val currency: String = "$",
    val phone: String = "",
    val address: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = false)]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String,
    val sku: String,
    val category: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val tax: Double = 0.0, // Percentage
    val currentStock: Int,
    val minimumStock: Int,
    val image: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentStock <= minimumStock
}

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val whatsapp: String,
    val address: String,
    val notes: String,
    val outstandingBalance: Double = 0.0
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val whatsapp: String,
    val notes: String
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String? = null,
    val date: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val total: Double,
    val paymentMethod: String, // "Cash", "Card", "UPI"
    val paymentStatus: String, // "Paid", "Pending"
    val notes: String,
    val createdBy: String // Username or Role
)

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val discount: Double = 0.0
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long? = null,
    val supplierName: String? = null,
    val date: Long = System.currentTimeMillis(),
    val total: Double,
    val notes: String,
    val createdBy: String
)

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val cost: Double
)

@Entity(tableName = "stock_history")
data class StockHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val change: Int, // positive for stock-in, negative for stock-out
    val reason: String, // "Initial Stock", "Adjustment", "Damaged", "Billing", "Purchase"
    val timestamp: Long = System.currentTimeMillis(),
    val user: String
)
