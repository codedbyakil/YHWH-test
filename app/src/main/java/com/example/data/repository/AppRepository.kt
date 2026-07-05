package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AppRepository(private val database: AppDatabase) {

    // DAOs
    private val businessDao = database.businessDao()
    private val userDao = database.userDao()
    private val productDao = database.productDao()
    private val supplierDao = database.supplierDao()
    private val customerDao = database.customerDao()
    private val billDao = database.billDao()
    private val purchaseDao = database.purchaseDao()
    private val stockHistoryDao = database.stockHistoryDao()

    // Business
    val business: Flow<Business?> = businessDao.getBusiness()
    suspend fun getBusinessOneShot() = businessDao.getBusinessOneShot()
    suspend fun saveBusiness(business: Business) = businessDao.insertBusiness(business)
    suspend fun updateBusiness(business: Business) = businessDao.updateBusiness(business)

    // Users
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User) = userDao.insertUser(user)

    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val activeProducts: Flow<List<Product>> = productDao.getActiveProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()

    fun searchProducts(query: String): Flow<List<Product>> {
        if (query.isBlank()) return productDao.getAllProducts()
        return productDao.searchProducts("%$query%")
    }

    suspend fun getProductById(id: Long) = productDao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String) = productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product): Long {
        val id = productDao.insertProduct(product)
        // Log stock addition
        stockHistoryDao.insertStockHistory(
            StockHistory(
                productId = id,
                productName = product.name,
                change = product.currentStock,
                reason = "Initial Stock",
                user = "System"
            )
        )
        return id
    }

    suspend fun updateProduct(product: Product, user: String = "User") {
        val existing = productDao.getProductById(product.id)
        productDao.updateProduct(product)
        if (existing != null && existing.currentStock != product.currentStock) {
            val change = product.currentStock - existing.currentStock
            stockHistoryDao.insertStockHistory(
                StockHistory(
                    productId = product.id,
                    productName = product.name,
                    change = change,
                    reason = "Stock Adjustment",
                    user = user
                )
            )
        }
    }

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun adjustStockManually(productId: Long, currentStock: Int, change: Int, reason: String, user: String) {
        val newStock = currentStock + change
        productDao.updateStock(productId, newStock)
        val product = productDao.getProductById(productId)
        stockHistoryDao.insertStockHistory(
            StockHistory(
                productId = productId,
                productName = product?.name ?: "Unknown Product",
                change = change,
                reason = reason,
                user = user
            )
        )
    }

    // Suppliers
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    suspend fun getSupplierById(id: Long) = supplierDao.getSupplierById(id)
    suspend fun insertSupplier(supplier: Supplier) = supplierDao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = supplierDao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.deleteSupplier(supplier)

    // Customers
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    suspend fun getCustomerById(id: Long) = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Bills (Sales Workflow)
    val allBills: Flow<List<Bill>> = billDao.getAllBills()
    suspend fun getBillById(id: Long) = billDao.getBillById(id)
    suspend fun getBillItemsByBillId(billId: Long) = billDao.getBillItemsByBillId(billId)

    suspend fun createBill(bill: Bill, items: List<BillItem>, user: String): Long {
        // We'll perform database updates. Room handles these nicely.
        val billId = billDao.insertBill(bill)
        val itemsWithBillId = items.map { it.copy(billId = billId) }
        billDao.insertBillItems(itemsWithBillId)

        // Reduce stocks and log stock history for each item
        for (item in itemsWithBillId) {
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                val newStock = product.currentStock - item.quantity
                productDao.updateStock(product.id, newStock)
                stockHistoryDao.insertStockHistory(
                    StockHistory(
                        productId = product.id,
                        productName = product.name,
                        change = -item.quantity,
                        reason = "Billing (Bill #${billId})",
                        user = user
                    )
                )
            }
        }
        return billId
    }

    // Purchases (Stock-In Workflow)
    val allPurchases: Flow<List<Purchase>> = purchaseDao.getAllPurchases()
    suspend fun getPurchaseById(id: Long) = purchaseDao.getPurchaseById(id)
    suspend fun getPurchaseItemsByPurchaseId(purchaseId: Long) = purchaseDao.getPurchaseItemsByPurchaseId(purchaseId)

    suspend fun createPurchase(purchase: Purchase, items: List<PurchaseItem>, user: String): Long {
        val purchaseId = purchaseDao.insertPurchase(purchase)
        val itemsWithPurchaseId = items.map { it.copy(purchaseId = purchaseId) }
        purchaseDao.insertPurchaseItems(itemsWithPurchaseId)

        // Increase stocks and log stock history for each item
        for (item in itemsWithPurchaseId) {
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                val newStock = product.currentStock + item.quantity
                productDao.updateStock(product.id, newStock)
                stockHistoryDao.insertStockHistory(
                    StockHistory(
                        productId = product.id,
                        productName = product.name,
                        change = item.quantity,
                        reason = "Purchase (Order #${purchaseId})",
                        user = user
                    )
                )
            }
        }

        // If supplier outstanding balance is tracked, update it here if needed
        if (purchase.supplierId != null) {
            val supplier = supplierDao.getSupplierById(purchase.supplierId)
            if (supplier != null) {
                val newBalance = supplier.outstandingBalance + purchase.total
                supplierDao.updateSupplier(supplier.copy(outstandingBalance = newBalance))
            }
        }

        return purchaseId
    }

    // Stock History
    val allStockHistory: Flow<List<StockHistory>> = stockHistoryDao.getAllStockHistory()
    fun getStockHistoryForProduct(productId: Long) = stockHistoryDao.getStockHistoryForProduct(productId)
}
