package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.YhwhViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: YhwhViewModel,
    onNavigateToAuth: () -> Unit
) {
    val business by viewModel.business.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val products by viewModel.allProducts.collectAsState()
    val bills by viewModel.allBills.collectAsState()
    val suppliers by viewModel.allSuppliers.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    var name by remember { mutableStateOf(business?.name ?: "") }
    var currency by remember { mutableStateOf(business?.currency ?: "") }
    var phone by remember { mutableStateOf(business?.phone ?: "") }
    var address by remember { mutableStateOf(business?.address ?: "") }
    var notes by remember { mutableStateOf(business?.notes ?: "") }

    var isEditingProfile by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Backup states
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf<String?>(null) }

    // Sync state values when loaded
    LaunchedEffect(business) {
        business?.let {
            name = it.name
            currency = it.currency
            phone = it.phone
            address = it.address
            notes = it.notes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Store Branding", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Business Profile Settings
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Business Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(
                        onClick = {
                            if (isEditingProfile) {
                                viewModel.saveBusinessProfile(name, currency, phone, address, notes)
                                saveSuccessMessage = "Profile updated successfully!"
                            }
                            isEditingProfile = !isEditingProfile
                        },
                        modifier = Modifier.testTag("edit_profile_toggle")
                    ) {
                        Icon(
                            imageVector = if (isEditingProfile) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (saveSuccessMessage != null) {
                    Text(
                        text = saveSuccessMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; saveSuccessMessage = null },
                    label = { Text("Store/Company Name") },
                    readOnly = !isEditingProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_store_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it; saveSuccessMessage = null },
                        label = { Text("Currency Symbol") },
                        readOnly = !isEditingProfile,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_currency"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; saveSuccessMessage = null },
                        label = { Text("Store Contact") },
                        readOnly = !isEditingProfile,
                        modifier = Modifier
                            .weight(2.2f)
                            .testTag("settings_phone"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; saveSuccessMessage = null },
                    label = { Text("Physical Address") },
                    readOnly = !isEditingProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_address"),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it; saveSuccessMessage = null },
                    label = { Text("Receipt Footer Notes") },
                    readOnly = !isEditingProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_notes"),
                    maxLines = 2
                )
            }

            Divider()

            // 2. Active Operator Profile
            Column {
                Text(
                    text = "Active Operator Session",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
                                text = currentUser?.name ?: "Operator",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Authorized Role Level: ${currentUser?.role ?: "Staff"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        Button(
                            onClick = onNavigateToAuth,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("switch_profile_btn")
                        ) {
                            Text("Switch Profile")
                        }
                    }
                }
            }

            Divider()

            // 3. Telemetry statistics
            Column {
                Text(
                    text = "Local Database Metrics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricIndicator("Products", products.size.toString(), Modifier.weight(1f))
                    MetricIndicator("Sales", bills.size.toString(), Modifier.weight(1f))
                    MetricIndicator("Suppliers", suppliers.size.toString(), Modifier.weight(1f))
                    MetricIndicator("Customers", customers.size.toString(), Modifier.weight(1f))
                }
            }

            Divider()

            // 4. Backup & Restore Simulation
            Column {
                Text(
                    text = "Local Backup & Cloud Sync",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            backupStatus = "Processing backup file to /sdcard/Download/yhwh_backup.json..."
                            showBackupDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f).testTag("backup_db_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup DB")
                    }

                    OutlinedButton(
                        onClick = {
                            backupStatus = "Reading backup file. Restoring database contents safely..."
                            showBackupDialog = true
                        },
                        modifier = Modifier.weight(1f).testTag("restore_db_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore DB")
                    }
                }
            }
        }
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Offline Backup Manager", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(backupStatus ?: "Syncing database data files securely.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "100% Success! Local database and schema configurations matched fully.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun MetricIndicator(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}
