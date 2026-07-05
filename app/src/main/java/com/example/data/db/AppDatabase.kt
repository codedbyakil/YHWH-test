package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Business::class,
        Product::class,
        Supplier::class,
        Customer::class,
        Bill::class,
        BillItem::class,
        Purchase::class,
        PurchaseItem::class,
        StockHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun customerDao(): CustomerDao
    abstract fun billDao(): BillDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun stockHistoryDao(): StockHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yhwh_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(db: AppDatabase) {
            // Seed a default business
            val businessId = db.businessDao().insertBusiness(
                Business(
                    name = "YHWH Supermart",
                    logo = null,
                    currency = "$",
                    phone = "+1 555-0199",
                    address = "777 Grace Avenue, Salem",
                    notes = "Your One-Stop Shop for Quality Goods"
                )
            )

            // Seed users
            db.userDao().insertUser(
                User(
                    email = "owner@yhwh.com",
                    phone = "+1 555-1234",
                    role = "Owner",
                    name = "John Doe",
                    businessId = businessId
                )
            )
            db.userDao().insertUser(
                User(
                    email = "staff@yhwh.com",
                    phone = "+1 555-5678",
                    role = "Staff",
                    name = "Jane Smith",
                    businessId = businessId
                )
            )

            // Seed Suppliers
            val sup1 = db.supplierDao().insertSupplier(
                Supplier(
                    name = "Alpha Wholesalers",
                    phone = "555-0111",
                    whatsapp = "555-0111",
                    address = "12 Warehouse Blvd, Industrial Zone",
                    notes = "Suppliers of groceries and FMCG goods.",
                    outstandingBalance = 250.0
                )
            )
            val sup2 = db.supplierDao().insertSupplier(
                Supplier(
                    name = "Omega Electronics",
                    phone = "555-0222",
                    whatsapp = "555-0222",
                    address = "42 Tech Plaza",
                    notes = "Suppliers of accessories, chargers, and mobile spares.",
                    outstandingBalance = 0.0
                )
            )

            // Seed Customers
            val cust1 = db.customerDao().insertCustomer(
                Customer(
                    name = "Walk-in Customer",
                    phone = "",
                    whatsapp = "",
                    notes = "Default client profile for quick walk-in sales."
                )
            )
            val cust2 = db.customerDao().insertCustomer(
                Customer(
                    name = "Alice Cooper",
                    phone = "555-0333",
                    whatsapp = "555-0333",
                    notes = "Regular customer, prefers messaging bills via WhatsApp."
                )
            )
            val cust3 = db.customerDao().insertCustomer(
                Customer(
                    name = "Robert Johnson",
                    phone = "555-0444",
                    whatsapp = "555-0444",
                    notes = "Local business client."
                )
            )

            // Seed Products
            val p1Id = db.productDao().insertProduct(
                Product(
                    name = "Organic Whole Milk 1L",
                    barcode = "40012345",
                    sku = "MK-ORG-1L",
                    category = "Groceries",
                    purchasePrice = 1.20,
                    sellingPrice = 1.99,
                    tax = 5.0,
                    currentStock = 45,
                    minimumStock = 10
                )
            )
            val p2Id = db.productDao().insertProduct(
                Product(
                    name = "Premium Wheat Bread",
                    barcode = "40012346",
                    sku = "BD-PRM-WHT",
                    category = "Groceries",
                    purchasePrice = 0.80,
                    sellingPrice = 1.49,
                    tax = 0.0,
                    currentStock = 8, // Low Stock Alert
                    minimumStock = 15
                )
            )
            val p3Id = db.productDao().insertProduct(
                Product(
                    name = "Sugar Refined 1kg",
                    barcode = "40012347",
                    sku = "SG-REF-1K",
                    category = "Groceries",
                    purchasePrice = 0.60,
                    sellingPrice = 1.10,
                    tax = 5.0,
                    currentStock = 120,
                    minimumStock = 20
                )
            )
            val p4Id = db.productDao().insertProduct(
                Product(
                    name = "USB-C Charging Cable 1.5m",
                    barcode = "40012348",
                    sku = "EL-USBC-1.5",
                    category = "Electronics",
                    purchasePrice = 3.50,
                    sellingPrice = 8.99,
                    tax = 18.0,
                    currentStock = 25,
                    minimumStock = 5
                )
            )
            val p5Id = db.productDao().insertProduct(
                Product(
                    name = "Wireless Charging Pad",
                    barcode = "40012349",
                    sku = "EL-WRLS-CHG",
                    category = "Electronics",
                    purchasePrice = 7.00,
                    sellingPrice = 19.99,
                    tax = 18.0,
                    currentStock = 3, // Low Stock Alert
                    minimumStock = 8
                )
            )

            // Seed Stock History
            db.stockHistoryDao().insertStockHistory(StockHistory(productId = p1Id, productName = "Organic Whole Milk 1L", change = 45, reason = "Initial Stock", user = "System"))
            db.stockHistoryDao().insertStockHistory(StockHistory(productId = p2Id, productName = "Premium Wheat Bread", change = 8, reason = "Initial Stock", user = "System"))
            db.stockHistoryDao().insertStockHistory(StockHistory(productId = p3Id, productName = "Sugar Refined 1kg", change = 120, reason = "Initial Stock", user = "System"))
            db.stockHistoryDao().insertStockHistory(StockHistory(productId = p4Id, productName = "USB-C Charging Cable 1.5m", change = 25, reason = "Initial Stock", user = "System"))
            db.stockHistoryDao().insertStockHistory(StockHistory(productId = p5Id, productName = "Wireless Charging Pad", change = 3, reason = "Initial Stock", user = "System"))

            // Seed historical bills and purchases to populate dashboard metrics nicely
            val billId1 = db.billDao().insertBill(
                Bill(
                    customerId = cust2,
                    customerName = "Alice Cooper",
                    date = System.currentTimeMillis() - 2 * 3600 * 1000, // 2 hours ago
                    subtotal = 3.48,
                    discount = 0.0,
                    tax = 0.10,
                    total = 3.58,
                    paymentMethod = "UPI",
                    paymentStatus = "Paid",
                    notes = "Fast checkout",
                    createdBy = "Staff"
                )
            )
            db.billDao().insertBillItems(
                listOf(
                    BillItem(billId = billId1, productId = p1Id, productName = "Organic Whole Milk 1L", quantity = 1, price = 1.99),
                    BillItem(billId = billId1, productId = p2Id, productName = "Premium Wheat Bread", quantity = 1, price = 1.49)
                )
            )

            val billId2 = db.billDao().insertBill(
                Bill(
                    customerId = cust1,
                    customerName = "Walk-in Customer",
                    date = System.currentTimeMillis() - 4 * 3600 * 1000, // 4 hours ago
                    subtotal = 17.98,
                    discount = 1.0,
                    tax = 3.24,
                    total = 20.22,
                    paymentMethod = "Cash",
                    paymentStatus = "Paid",
                    notes = "Cash discount given",
                    createdBy = "Owner"
                )
            )
            db.billDao().insertBillItems(
                listOf(
                    BillItem(billId = billId2, productId = p4Id, productName = "USB-C Charging Cable 1.5m", quantity = 2, price = 8.99)
                )
            )

            // Historical Purchases
            val purchaseId1 = db.purchaseDao().insertPurchase(
                Purchase(
                    supplierId = sup1,
                    supplierName = "Alpha Wholesalers",
                    date = System.currentTimeMillis() - 24 * 3600 * 1000, // Yesterday
                    total = 100.0,
                    notes = "Bulk grocery restock",
                    createdBy = "Owner"
                )
            )
            db.purchaseDao().insertPurchaseItems(
                listOf(
                    PurchaseItem(purchaseId = purchaseId1, productId = p1Id, productName = "Organic Whole Milk 1L", quantity = 50, cost = 1.20),
                    PurchaseItem(purchaseId = purchaseId1, productId = p3Id, productName = "Sugar Refined 1kg", quantity = 50, cost = 0.60)
                )
            )
        }
    }
}
