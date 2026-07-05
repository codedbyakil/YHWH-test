package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.YhwhViewModel
import com.example.data.db.Product
import com.example.data.db.StockHistory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    viewModel: YhwhViewModel
) {
    val products by viewModel.allProducts.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val history by viewModel.allStockHistory.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Stock levels, 1: Adjustment, 2: History Log
    var showAdjustmentDialog by remember { mutableStateOf(false) }
    var selectedProductToAdjust by remember { mutableStateOf<Product?>(null) }

    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock & Inventory Audit", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Levels", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("stock_tab_levels")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Adjustments", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("stock_tab_adjust")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("History Ledger", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("stock_tab_history")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (activeTab) {
                0 -> {
                    // Live Stock Levels Screen
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Current Inventory Levels",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(products) { prod ->
                            val isLow = prod.isLowStock
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "SKU: ${prod.sku} • Min alert: ${prod.minimumStock}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isLow) MaterialTheme.colorScheme.errorContainer
                                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${prod.currentStock} in stock",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isLow) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Stock Adjustments / Damaged entries selection
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Select a product to write off or adjust stock:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(products) { prod ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedProductToAdjust = prod
                                        showAdjustmentDialog = true
                                    }
                                    .testTag("adjust_prod_${prod.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Current: ${prod.currentStock} | Min: ${prod.minimumStock}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Icon(imageVector = Icons.Default.Tune, contentDescription = "Adjust", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Persistent History Log / Ledger
                    if (history.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No stock log records exist yet.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(history) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = log.productName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Reason: ${log.reason} • By: ${log.user}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = formatter.format(Date(log.timestamp)),
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }

                                        val isPositive = log.change >= 0
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isPositive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isPositive) "+${log.change}" else "${log.change}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual adjustment dialog
    if (showAdjustmentDialog && selectedProductToAdjust != null) {
        val prod = selectedProductToAdjust!!
        var adjustQtyInput by remember { mutableStateOf("") }
        var isAddition by remember { mutableStateOf(true) } // true for addition, false for reduction
        var reason by remember { mutableStateOf("Adjustment") } // "Adjustment", "Damaged", "Lost", "Restock"
        var errorText by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAdjustmentDialog = false },
            title = { Text("Stock Level Adjuster", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Adjusting stock for: ${prod.name}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(text = "Current Stock Level: ${prod.currentStock}", style = MaterialTheme.typography.bodyMedium)

                    if (errorText != null) {
                        Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isAddition = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAddition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f).testTag("adjust_add_toggle")
                        ) {
                            Text("Stock IN (+)", color = if (isAddition) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { isAddition = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isAddition) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f).testTag("adjust_sub_toggle")
                        ) {
                            Text("Stock OUT (-)", color = if (!isAddition) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    OutlinedTextField(
                        value = adjustQtyInput,
                        onValueChange = { adjustQtyInput = it },
                        label = { Text("Quantity count to change*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("adjust_qty_input")
                    )

                    Column {
                        Text(text = "Adjustment Reason", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Adjustment", "Damaged", "Lost").forEach { item ->
                                val isSelected = reason == item
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { reason = item }
                                        .padding(vertical = 8.dp)
                                        .testTag("adjust_reason_$item"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = adjustQtyInput.toIntOrNull()
                        if (qty == null || qty <= 0) {
                            errorText = "Please enter a valid positive number"
                            return@Button
                        }

                        val change = if (isAddition) qty else -qty
                        viewModel.adjustStock(
                            productId = prod.id,
                            currentStock = prod.currentStock,
                            change = change,
                            reason = reason
                        )
                        showAdjustmentDialog = false
                    },
                    modifier = Modifier.testTag("adjust_confirm_btn")
                ) {
                    Text("Apply Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustmentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
