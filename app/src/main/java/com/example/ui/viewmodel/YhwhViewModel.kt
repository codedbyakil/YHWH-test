package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.YhwhApplication
import com.example.data.db.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class YhwhViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // Current State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Database flows
    val business: StateFlow<Business?> = repository.business.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allUsers: StateFlow<List<User>> = repository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts: StateFlow<List<Product>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeProducts: StateFlow<List<Product>> = repository.activeProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSuppliers: StateFlow<List<Supplier>> = repository.allSuppliers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBills: StateFlow<List<Bill>> = repository.allBills.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPurchases: StateFlow<List<Purchase>> = repository.allPurchases.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allStockHistory: StateFlow<List<StockHistory>> = repository.allStockHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Searched products flow
    val searchedProducts: StateFlow<List<Product>> = _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            repository.searchProducts(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Billing State
    private val _billingCart = MutableStateFlow<List<CartItem>>(emptyList())
    val billingCart: StateFlow<List<CartItem>> = _billingCart.asStateFlow()

    private val _billingCustomer = MutableStateFlow<Customer?>(null)
    val billingCustomer: StateFlow<Customer?> = _billingCustomer.asStateFlow()

    private val _billingDiscount = MutableStateFlow(0.0)
    val billingDiscount: StateFlow<Double> = _billingDiscount.asStateFlow()

    private val _billingPaymentMethod = MutableStateFlow("Cash") // "Cash", "Card", "UPI"
    val billingPaymentMethod: StateFlow<String> = _billingPaymentMethod.asStateFlow()

    private val _billingNotes = MutableStateFlow("")
    val billingNotes: StateFlow<String> = _billingNotes.asStateFlow()

    // Current Purchase State
    private val _purchaseCart = MutableStateFlow<List<PurchaseCartItem>>(emptyList())
    val purchaseCart: StateFlow<List<PurchaseCartItem>> = _purchaseCart.asStateFlow()

    private val _purchaseSupplier = MutableStateFlow<Supplier?>(null)
    val purchaseSupplier: StateFlow<Supplier?> = _purchaseSupplier.asStateFlow()

    private val _purchaseNotes = MutableStateFlow("")
    val purchaseNotes: StateFlow<String> = _purchaseNotes.asStateFlow()

    // Active Generated Receipt (for billing receipt screen)
    private val _activeReceipt = MutableStateFlow<ReceiptData?>(null)
    val activeReceipt: StateFlow<ReceiptData?> = _activeReceipt.asStateFlow()

    init {
        // Initialize with default Owner user once users are loaded
        viewModelScope.launch {
            allUsers.collect { users ->
                if (users.isNotEmpty() && _currentUser.value == null) {
                    _currentUser.value = users.firstOrNull { it.role == "Owner" } ?: users.first()
                }
            }
        }
    }

    // Authentication Functions
    fun switchUserRole(role: String) {
        val user = allUsers.value.firstOrNull { it.role == role }
        if (user != null) {
            _currentUser.value = user
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Business Profile Operations
    fun saveBusinessProfile(name: String, currency: String, phone: String, address: String, notes: String) {
        viewModelScope.launch {
            val current = business.value
            if (current != null) {
                repository.updateBusiness(
                    current.copy(
                        name = name,
                        currency = currency,
                        phone = phone,
                        address = address,
                        notes = notes
                    )
                )
            } else {
                repository.saveBusiness(
                    Business(
                        name = name,
                        currency = currency,
                        phone = phone,
                        address = address,
                        notes = notes
                    )
                )
            }
        }
    }

    // Product Operations
    fun addProduct(name: String, barcode: String, sku: String, category: String, purchasePrice: Double, sellingPrice: Double, tax: Double, currentStock: Int, minimumStock: Int) {
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    name = name,
                    barcode = barcode,
                    sku = sku,
                    category = category,
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    tax = tax,
                    currentStock = currentStock,
                    minimumStock = minimumStock
                )
            )
        }
    }

    fun updateProductDetails(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product, _currentUser.value?.role ?: "User")
        }
    }

    fun deleteProductDetails(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adjustStock(productId: Long, currentStock: Int, change: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustStockManually(
                productId = productId,
                currentStock = currentStock,
                change = change,
                reason = reason,
                user = _currentUser.value?.role ?: "User"
            )
        }
    }

    // Supplier Operations
    fun addSupplier(name: String, phone: String, whatsapp: String, address: String, notes: String, outstanding: Double) {
        viewModelScope.launch {
            repository.insertSupplier(
                Supplier(
                    name = name,
                    phone = phone,
                    whatsapp = whatsapp,
                    address = address,
                    notes = notes,
                    outstandingBalance = outstanding
                )
            )
        }
    }

    fun updateSupplierDetails(supplier: Supplier) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
        }
    }

    fun deleteSupplierDetails(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    // Customer Operations
    fun addCustomer(name: String, phone: String, whatsapp: String, notes: String) {
        viewModelScope.launch {
            repository.insertCustomer(
                Customer(
                    name = name,
                    phone = phone,
                    whatsapp = whatsapp,
                    notes = notes
                )
            )
        }
    }

    fun updateCustomerDetails(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomerDetails(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Billing Cart Functions
    fun addProductToBillingCart(product: Product, qty: Int = 1) {
        val currentList = _billingCart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + qty)
        } else {
            currentList.add(CartItem(product = product, quantity = qty, customPrice = product.sellingPrice, discount = 0.0))
        }
        _billingCart.value = currentList
    }

    fun updateCartItemQuantity(productId: Long, qty: Int) {
        if (qty <= 0) {
            removeProductFromBillingCart(productId)
            return
        }
        val currentList = _billingCart.value.map {
            if (it.product.id == productId) it.copy(quantity = qty) else it
        }
        _billingCart.value = currentList
    }

    fun removeProductFromBillingCart(productId: Long) {
        _billingCart.value = _billingCart.value.filterNot { it.product.id == productId }
    }

    fun setBillingCustomer(customer: Customer?) {
        _billingCustomer.value = customer
    }

    fun setBillingDiscount(discount: Double) {
        _billingDiscount.value = discount
    }

    fun setBillingPaymentMethod(method: String) {
        _billingPaymentMethod.value = method
    }

    fun setBillingNotes(notes: String) {
        _billingNotes.value = notes
    }

    fun clearBillingCart() {
        _billingCart.value = emptyList()
        _billingCustomer.value = null
        _billingDiscount.value = 0.0
        _billingPaymentMethod.value = "Cash"
        _billingNotes.value = ""
    }

    // Complete Billing Sale Transaction
    fun checkoutBill(onSuccess: (Long) -> Unit) {
        val cart = _billingCart.value
        if (cart.isEmpty()) return

        viewModelScope.launch {
            val customer = _billingCustomer.value
            val subtotal = cart.sumOf { it.customPrice * it.quantity }
            val discountVal = _billingDiscount.value
            val taxTotal = cart.sumOf { item ->
                val itemSub = item.customPrice * item.quantity
                itemSub * (item.product.tax / 100.0)
            }
            val total = subtotal - discountVal + taxTotal

            val bill = Bill(
                customerId = customer?.id,
                customerName = customer?.name ?: "Walk-in Customer",
                subtotal = subtotal,
                discount = discountVal,
                tax = taxTotal,
                total = if (total < 0) 0.0 else total,
                paymentMethod = _billingPaymentMethod.value,
                paymentStatus = "Paid",
                notes = _billingNotes.value,
                createdBy = _currentUser.value?.role ?: "Staff"
            )

            val billItems = cart.map { item ->
                BillItem(
                    billId = 0, // Assigned later by Repository
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    price = item.customPrice,
                    discount = item.discount
                )
            }

            val billId = repository.createBill(bill, billItems, _currentUser.value?.role ?: "Staff")

            // Store active receipt state for rendering the receipt screen
            _activeReceipt.value = ReceiptData(
                bill = bill.copy(id = billId),
                items = billItems,
                customer = customer,
                businessName = business.value?.name ?: "YHWH Shop",
                currency = business.value?.currency ?: "$"
            )

            // Clear Cart
            clearBillingCart()
            onSuccess(billId)
        }
    }

    // Purchase Cart Functions
    fun addProductToPurchaseCart(product: Product, qty: Int = 1) {
        val currentList = _purchaseCart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + qty)
        } else {
            currentList.add(PurchaseCartItem(product = product, quantity = qty, customCost = product.purchasePrice))
        }
        _purchaseCart.value = currentList
    }

    fun updatePurchaseCartItemQty(productId: Long, qty: Int) {
        if (qty <= 0) {
            removeProductFromPurchaseCart(productId)
            return
        }
        val currentList = _purchaseCart.value.map {
            if (it.product.id == productId) it.copy(quantity = qty) else it
        }
        _purchaseCart.value = currentList
    }

    fun removeProductFromPurchaseCart(productId: Long) {
        _purchaseCart.value = _purchaseCart.value.filterNot { it.product.id == productId }
    }

    fun setPurchaseSupplier(supplier: Supplier?) {
        _purchaseSupplier.value = supplier
    }

    fun setPurchaseNotes(notes: String) {
        _purchaseNotes.value = notes
    }

    fun clearPurchaseCart() {
        _purchaseCart.value = emptyList()
        _purchaseSupplier.value = null
        _purchaseNotes.value = ""
    }

    // Complete Purchase Order Transaction
    fun checkoutPurchase(onSuccess: (Long) -> Unit) {
        val cart = _purchaseCart.value
        if (cart.isEmpty()) return

        viewModelScope.launch {
            val supplier = _purchaseSupplier.value
            val total = cart.sumOf { it.customCost * it.quantity }

            val purchase = Purchase(
                supplierId = supplier?.id,
                supplierName = supplier?.name ?: "Direct Purchase",
                total = total,
                notes = _purchaseNotes.value,
                createdBy = _currentUser.value?.role ?: "Owner"
            )

            val purchaseItems = cart.map { item ->
                PurchaseItem(
                    purchaseId = 0,
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    cost = item.customCost
                )
            }

            val purchaseId = repository.createPurchase(purchase, purchaseItems, _currentUser.value?.role ?: "Owner")

            // Clear Cart
            clearPurchaseCart()
            onSuccess(purchaseId)
        }
    }
}

// Support Data Classes
data class CartItem(
    val product: Product,
    val quantity: Int,
    val customPrice: Double,
    val discount: Double
)

data class PurchaseCartItem(
    val product: Product,
    val quantity: Int,
    val customCost: Double
)

data class ReceiptData(
    val bill: Bill,
    val items: List<BillItem>,
    val customer: Customer?,
    val businessName: String,
    val currency: String
)

// ViewModel Factory
class YhwhViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YhwhViewModel::class.java)) {
            return YhwhViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
