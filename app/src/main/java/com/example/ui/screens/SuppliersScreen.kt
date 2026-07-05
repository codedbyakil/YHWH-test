package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.YhwhViewModel
import com.example.data.db.Supplier
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    viewModel: YhwhViewModel
) {
    val suppliers by viewModel.allSuppliers.collectAsState()
    val business by viewModel.business.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val currencySymbol = business?.currency ?: "$"

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }
    var supplierSearchQuery by remember { mutableStateOf("") }

    val filteredSuppliers = remember(suppliers, supplierSearchQuery) {
        if (supplierSearchQuery.isBlank()) {
            suppliers
        } else {
            suppliers.filter { it.name.contains(supplierSearchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supplier Directory", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        editingSupplier = null
                        showAddEditDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Supplier")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingSupplier = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_supplier_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Supplier")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = supplierSearchQuery,
                onValueChange = { supplierSearchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("supplier_search_bar"),
                placeholder = { Text("Search suppliers by name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredSuppliers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No suppliers on record",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSuppliers) { supplier ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingSupplier = supplier
                                    showAddEditDialog = true
                                }
                                .testTag("supplier_item_${supplier.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = supplier.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (supplier.outstandingBalance > 0) MaterialTheme.colorScheme.errorContainer
                                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Debt: $currencySymbol${String.format(Locale.getDefault(), "%.2f", supplier.outstandingBalance)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (supplier.outstandingBalance > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = supplier.phone.ifBlank { "No number listed" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    if (supplier.whatsapp.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp Link", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "WhatsApp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                if (supplier.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = supplier.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditSupplierDialog(
            supplier = editingSupplier,
            currencySymbol = currencySymbol,
            canDelete = currentUser?.role == "Owner",
            onDismiss = { showAddEditDialog = false },
            onSave = { name, phone, whatsapp, address, notes, outstanding ->
                if (editingSupplier != null) {
                    viewModel.updateSupplierDetails(
                        editingSupplier!!.copy(
                            name = name,
                            phone = phone,
                            whatsapp = whatsapp,
                            address = address,
                            notes = notes,
                            outstandingBalance = outstanding
                        )
                    )
                } else {
                    viewModel.addSupplier(name, phone, whatsapp, address, notes, outstanding)
                }
                showAddEditDialog = false
            },
            onDelete = {
                editingSupplier?.let { viewModel.deleteSupplierDetails(it) }
                showAddEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSupplierDialog(
    supplier: Supplier?,
    currencySymbol: String,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, whatsapp: String, address: String, notes: String, outstanding: Double) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var whatsapp by remember { mutableStateOf(supplier?.whatsapp ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }
    var notes by remember { mutableStateOf(supplier?.notes ?: "") }
    var outstanding by remember { mutableStateOf(supplier?.outstandingBalance?.toString() ?: "0.0") }

    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (supplier != null) "Edit Supplier Contact" else "New Supplier Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Supplier Name*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_supplier_name"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_supplier_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_supplier_whatsapp"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Office Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_supplier_address"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = outstanding,
                    onValueChange = { outstanding = it },
                    label = { Text("Outstanding Balance ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_supplier_outstanding"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Categories handled, etc.)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_supplier_notes"),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorText = "Please enter supplier name"
                        return@Button
                    }
                    val balance = outstanding.toDoubleOrNull() ?: 0.0
                    onSave(name, phone, whatsapp, address, notes, balance)
                },
                modifier = Modifier.testTag("dialog_save_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (supplier != null && canDelete) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("dialog_delete_btn")
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
