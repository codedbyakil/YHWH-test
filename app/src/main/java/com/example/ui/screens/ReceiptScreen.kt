package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.YhwhViewModel
import com.example.ui.viewmodel.ReceiptData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    viewModel: YhwhViewModel,
    onNavigateBackToBilling: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val receiptDataState by viewModel.activeReceipt.collectAsState()
    val context = LocalContext.current

    val formatter = remember { SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToBilling) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        val data = receiptDataState
        if (data == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No active receipt to show.", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onNavigateToDashboard) {
                        Text("Return to Dashboard")
                    }
                }
            }
        } else {
            val dateStr = formatter.format(Date(data.bill.date))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Receipt visual container (looks like a paper slip)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .testTag("receipt_slip_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Store Header
                                Text(
                                    text = data.businessName.uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "OFFICIAL SALES INVOICE",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Invoice Metadata
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Invoice: #${data.bill.id}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                // Customer Metadata
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "BILLED TO:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Text(text = data.bill.customerName ?: "Walk-in Customer", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        if (data.customer?.phone?.isNotBlank() == true) {
                                            Text(text = "Ph: ${data.customer.phone}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "OPERATOR:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Text(text = data.bill.createdBy, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                // Items Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "ITEM", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(2f))
                                    Text(text = "QTY", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(text = "PRICE", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    Text(text = "TOTAL", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Items List
                                data.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = item.productName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f), maxLines = 1)
                                        Text(text = "${item.quantity}x", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                        Text(text = String.format(Locale.getDefault(), "%.2f", item.price), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                        Text(text = String.format(Locale.getDefault(), "%.2f", item.price * item.quantity), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                // Summary Computations
                                SummaryRow("Subtotal", String.format(Locale.getDefault(), "%s%.2f", data.currency, data.bill.subtotal))
                                if (data.bill.tax > 0) {
                                    SummaryRow("Tax Goods & Services", String.format(Locale.getDefault(), "%s%.2f", data.currency, data.bill.tax))
                                }
                                if (data.bill.discount > 0) {
                                    SummaryRow("Discount Deducted", String.format(Locale.getDefault(), "-%s%.2f", data.currency, data.bill.discount))
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "NET TOTAL DUE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%s%.2f", data.currency, data.bill.total),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "PAID VIA ${data.bill.paymentMethod.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Sharing & Navigation bottom triggers
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                shareReceiptToWhatsApp(context, data, dateStr)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("share_whatsapp_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Branding green!
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Text(
                                    text = "Send Receipt via WhatsApp",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToDashboard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("return_dashboard_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Return to Dashboard",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

// Format and send the receipt formatted text natively via share action
private fun shareReceiptToWhatsApp(context: Context, data: ReceiptData, dateStr: String) {
    val sb = StringBuilder()
    sb.append("🛍️ *${data.businessName.uppercase()}* 🛍️\n")
    sb.append("------------------------------------------\n")
    sb.append("📄 *INVOICE:* #${data.bill.id}\n")
    sb.append("📅 *DATE:* $dateStr\n")
    sb.append("------------------------------------------\n")
    sb.append("👤 *CUSTOMER:* ${data.bill.customerName ?: "Walk-in Customer"}\n")
    sb.append("------------------------------------------\n\n")

    val billItems = data.items
    billItems.forEach { item ->
        val itemTotal = item.price * item.quantity
        sb.append("▪️ *${item.productName}*\n")
        sb.append("   ${item.quantity} x ${data.currency}${String.format(Locale.getDefault(), "%.2f", item.price)} = *${data.currency}${String.format(Locale.getDefault(), "%.2f", itemTotal)}*\n")
    }

    sb.append("\n------------------------------------------\n")
    sb.append("💵 *SUBTOTAL:* ${data.currency}${String.format(Locale.getDefault(), "%.2f", data.bill.subtotal)}\n")
    if (data.bill.tax > 0) {
        sb.append("⚖️ *TAX:* ${data.currency}${String.format(Locale.getDefault(), "%.2f", data.bill.tax)}\n")
    }
    if (data.bill.discount > 0) {
        sb.append("🔻 *DISCOUNT:* -${data.currency}${String.format(Locale.getDefault(), "%.2f", data.bill.discount)}\n")
    }
    sb.append("🏆 *GRAND TOTAL:* *${data.currency}${String.format(Locale.getDefault(), "%.2f", data.bill.total)}*\n")
    sb.append("------------------------------------------\n")
    sb.append("💳 *PAID VIA:* ${data.bill.paymentMethod.uppercase()}\n\n")
    sb.append("🙏 *Thank you for your business!* 🙏\n")
    sb.append("Powered by YHWH Platform")

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        // Attempt to specifically target WhatsApp if present (optional, falls back to full chooser nicely)
        `package` = "com.whatsapp"
    }

    try {
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        // WhatsApp not directly targeted or installed, show default chooser
        val chooserIntent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }, "Send Receipt invoice via:")
        context.startActivity(chooserIntent)
    }
}
