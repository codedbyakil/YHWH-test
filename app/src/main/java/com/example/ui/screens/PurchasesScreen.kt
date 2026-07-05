package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.YhwhViewModel
import com.example.data.db.Product
import com.example.data.db.Supplier
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    viewModel: YhwhViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddSupplier: () -> Unit
) {
    val activeProducts by viewModel.activeProducts.collectAsState()
    val allSuppliers by viewModel.allSuppliers.collectAsState()
    val business by viewModel.business.collectAsState()

    val cart by viewModel.purchaseCart.collectAsState()
    val selectedSupplier by viewModel.purchaseSupplier.collectAsState()
    val notes by viewModel.purchaseNotes.collectAsState()

    val currencySymbol = business?.currency ?: "$"

    var productSearchQuery by remember { mutableStateOf("") }
    var showSupplierSelector by remember { mutableStateOf(false) }

    // Derived total cost
    val totalCost = cart.sumOf { it.customCost * it.quantity }

    val filteredProducts = remember(activeProducts, productSearchQuery) {
        if (productSearchQuery.isBlank()) {
            activeProducts.take(6)
        } else {
            activeProducts.filter {
                it.name.contains(productSearchQuery, ignoreCase = true) ||
                        it.barcode.contains(productSearchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Supplier Purchase", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearPurchaseCart() }) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
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
                // 1. Supplier Selector
                item {
                    Text(
                        text = "Supplier Profile",
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
                                    text = selectedSupplier?.name ?: "Direct Purchases / No Supplier",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (selectedSupplier?.phone?.isNotBlank() == true) "Phone: ${selectedSupplier!!.phone}" else "Direct stock purchase, no supplier balance logged",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Button(
                                onClick = { showSupplierSelector = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("select_supplier_btn")
                            ) {
                                Text("Select")
                            }
                        }
                    }
                }

                // 2. Product Search
                item {
                    Text(
                        text = "Add Stock items",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text("Search products to add...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_product_search"),
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
                                    .clickable { viewModel.addProductToPurchaseCart(prod) }
                                    .testTag("purchase_quick_add_${prod.id}"),
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
                                        text = "Cost: ${currencySymbol}${prod.purchasePrice}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Current: ${prod.currentStock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Purchase Items Cart
                item {
                    Text(
                        text = "Purchase Orders (${cart.size})",
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
                                text = "No items added to purchase list.",
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
                                        text = "Unit Cost: ${currencySymbol}${cartItem.customCost} | Current Stock: ${cartItem.product.currentStock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updatePurchaseCartItemQty(cartItem.product.id, cartItem.quantity - 1) },
                                        modifier = Modifier.size(32.dp).testTag("purchase_dec_${cartItem.product.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        text = cartItem.quantity.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.testTag("purchase_qty_${cartItem.product.id}")
                                    )
                                    IconButton(
                                        onClick = { viewModel.updatePurchaseCartItemQty(cartItem.product.id, cartItem.quantity + 1) },
                                        modifier = Modifier.size(32.dp).testTag("purchase_inc_${cartItem.product.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Order notes
                if (cart.isNotEmpty()) {
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { viewModel.setPurchaseNotes(it) },
                            label = { Text("Purchase Notes (Invoice details, Batch codes, etc.)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("purchase_notes_field"),
                            maxLines = 2
                        )
                    }
                }
            }

            // Checkout Summary sticky footer
            if (cart.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Investment Cost",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, totalCost),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.checkoutPurchase { purchaseId ->
                                        onNavigateBack()
                                    }
                                },
                                modifier = Modifier.testTag("submit_purchase_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete Order")
                            }
                        }
                    }
                }
            }
        }
    }

    // Supplier selector Dialog
    if (showSupplierSelector) {
        AlertDialog(
            onDismissRequest = { showSupplierSelector = false },
            title = { Text("Link Supplier Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.setPurchaseSupplier(null)
                            showSupplierSelector = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Direct Cash Purchase (No Account)")
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allSuppliers) { supplier ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setPurchaseSupplier(supplier)
                                        showSupplierSelector = false
                                    }
                                    .testTag("select_sup_${supplier.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = supplier.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "Outstanding Balance: $currencySymbol${supplier.outstandingBalance}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            showSupplierSelector = false
                            onNavigateToAddSupplier()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New Supplier Contact")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupplierSelector = false }) {
                    Text("Close")
                }
            }
        )
    }
}
