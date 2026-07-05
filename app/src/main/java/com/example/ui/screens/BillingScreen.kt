package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.YhwhViewModel
import com.example.data.db.Product
import com.example.data.db.Customer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: YhwhViewModel,
    onNavigateToReceipt: () -> Unit,
    onNavigateToAddCustomer: () -> Unit
) {
    val activeProducts by viewModel.activeProducts.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val business by viewModel.business.collectAsState()

    val cart by viewModel.billingCart.collectAsState()
    val selectedCustomer by viewModel.billingCustomer.collectAsState()
    val discount by viewModel.billingDiscount.collectAsState()
    val paymentMethod by viewModel.billingPaymentMethod.collectAsState()
    val notes by viewModel.billingNotes.collectAsState()

    val currencySymbol = business?.currency ?: "$"

    var productSearchQuery by remember { mutableStateOf("") }
    var showBarcodeSimulator by remember { mutableStateOf(false) }
    var barcodeSimulatedInput by remember { mutableStateOf("") }
    var barcodeError by remember { mutableStateOf<String?>(null) }

    var showCustomerSelector by remember { mutableStateOf(false) }
    var discountInput by remember { mutableStateOf(if (discount == 0.0) "" else discount.toString()) }

    // Derived checkout sums
    val subtotal = cart.sumOf { it.customPrice * it.quantity }
    val taxTotal = cart.sumOf { item ->
        val itemSub = item.customPrice * item.quantity
        itemSub * (item.product.tax / 100.0)
    }
    val finalTotal = subtotal - discount + taxTotal

    // Filter products for quick selection
    val filteredProducts = remember(activeProducts, productSearchQuery) {
        if (productSearchQuery.isBlank()) {
            activeProducts.take(6) // Quick selection items
        } else {
            activeProducts.filter {
                it.name.contains(productSearchQuery, ignoreCase = true) ||
                        it.barcode.contains(productSearchQuery, ignoreCase = true) ||
                        it.sku.contains(productSearchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Sales Bill", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.clearBillingCart(); discountInput = "" }) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Cart", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Customer Selector Row
                item {
                    Text(
                        text = "Customer Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCustomer?.name ?: "Walk-in Customer",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (selectedCustomer?.phone?.isNotBlank() == true) "Phone: ${selectedCustomer!!.phone}" else "No contact profile linked",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Row {
                                Button(
                                    onClick = { showCustomerSelector = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("select_customer_btn")
                                ) {
                                    Text("Change")
                                }
                            }
                        }
                    }
                }

                // 2. Barcode Scanner Simulation Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Smart Barcode Scanner Simulator",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Switch(
                                    checked = showBarcodeSimulator,
                                    onCheckedChange = { showBarcodeSimulator = it },
                                    modifier = Modifier.testTag("barcode_toggle")
                                )
                            }

                            if (showBarcodeSimulator) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Scan or type a pre-seeded barcode (e.g. 40012345, 40012346, 40012347, 40012348, 40012349):",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = barcodeSimulatedInput,
                                        onValueChange = {
                                            barcodeSimulatedInput = it
                                            barcodeError = null
                                        },
                                        placeholder = { Text("Enter Barcode") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("barcode_sim_input"),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            if (barcodeSimulatedInput.isNotBlank()) {
                                                val found = activeProducts.firstOrNull { it.barcode == barcodeSimulatedInput }
                                                if (found != null) {
                                                    viewModel.addProductToBillingCart(found)
                                                    barcodeSimulatedInput = ""
                                                    barcodeError = null
                                                } else {
                                                    barcodeError = "Product not found!"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("scan_trigger_btn")
                                    ) {
                                        Text("Scan")
                                    }
                                }
                                if (barcodeError != null) {
                                    Text(
                                        text = barcodeError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Product Search & Quick add
                item {
                    Text(
                        text = "Add Products to Bill",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text("Search product name to add...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("billing_product_search"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { prod ->
                            Card(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { viewModel.addProductToBillingCart(prod) }
                                    .testTag("quick_add_prod_${prod.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, prod.sellingPrice),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Stock: ${prod.currentStock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (prod.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Cart List Rows
                item {
                    Text(
                        text = "Billing Items (${cart.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (cart.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Your billing cart is empty.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    items(cart) { cartItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cartItem.product.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Price: ${currencySymbol}${cartItem.customPrice} | Tax: ${cartItem.product.tax}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(cartItem.product.id, cartItem.quantity - 1) },
                                        modifier = Modifier.size(32.dp).testTag("cart_dec_${cartItem.product.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        text = cartItem.quantity.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.testTag("cart_qty_${cartItem.product.id}")
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(cartItem.product.id, cartItem.quantity + 1) },
                                        modifier = Modifier.size(32.dp).testTag("cart_inc_${cartItem.product.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Payment, Discounts & Notes Section
                if (cart.isNotEmpty()) {
                    item {
                        Text(
                            text = "Discounts & Payment Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = {
                                    discountInput = it
                                    val d = it.toDoubleOrNull() ?: 0.0
                                    viewModel.setBillingDiscount(d)
                                },
                                label = { Text("Flat Discount ($currencySymbol)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("billing_discount_field"),
                                singleLine = true
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pay Mode",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Cash", "Card", "UPI").forEach { method ->
                                        val isSelected = paymentMethod == method
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { viewModel.setBillingPaymentMethod(method) }
                                                .padding(vertical = 10.dp)
                                                .testTag("pay_method_$method"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = method,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Checkout Summary Sticky Footer Card
            if (cart.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Subtotal: ${currencySymbol}${String.format(Locale.getDefault(), "%.2f", subtotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Tax Total: ${currencySymbol}${String.format(Locale.getDefault(), "%.2f", taxTotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Discount: -${currencySymbol}${String.format(Locale.getDefault(), "%.2f", discount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Payable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, finalTotal),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.checkoutBill { billId ->
                                    onNavigateToReceipt()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("checkout_bill_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null)
                                Text(
                                    text = "Generate Invoice & Share",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Selector Dialog
    if (showCustomerSelector) {
        AlertDialog(
            onDismissRequest = { showCustomerSelector = false },
            title = { Text("Select Customer Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.setBillingCustomer(null)
                            showCustomerSelector = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Walk-in Customer (Default)")
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCustomers) { customer ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setBillingCustomer(customer)
                                        showCustomerSelector = false
                                    }
                                    .testTag("select_cust_${customer.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = customer.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                    if (customer.phone.isNotBlank()) {
                                        Text(text = "Phone: ${customer.phone}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            showCustomerSelector = false
                            onNavigateToAddCustomer()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New Customer Contact")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerSelector = false }) {
                    Text("Close")
                }
            }
        )
    }
}
