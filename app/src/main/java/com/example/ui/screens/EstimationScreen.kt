package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CuttingPart
import com.example.model.Estimation
import com.example.model.FurnitureConfig
import com.example.model.FurnitureProject
import com.example.model.MaterialRates
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import com.example.util.ShareHelper
import com.example.viewmodel.Screen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EstimationScreen(
    project: FurnitureProject?,
    config: FurnitureConfig,
    cuttingParts: List<CuttingPart>,
    estimation: Estimation,
    rates: MaterialRates,
    language: AppLanguage,
    onUpdateRates: (MaterialRates) -> Unit,
    onUpdateFinancials: (profitMarginPercent: Double, gstPercent: Double, advancePaid: Double) -> Unit,
    onNavigateToQuotation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    var showRatesDialog by remember { mutableStateOf(false) }
    var advanceText by remember(estimation.advancePaid) { mutableStateOf(estimation.advancePaid.toInt().toString()) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Prominent Grand Total Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("estimation_total_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = Localization.get("grand_total", language),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹ ${inrFormat.format(estimation.grandTotal).replace("₹", "").trim()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24) // Gold
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = Localization.get("advance_paid", language),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "₹ ${inrFormat.format(estimation.advancePaid).replace("₹", "").trim()}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Localization.get("remaining_balance", language),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "₹ ${inrFormat.format(estimation.balanceRemaining).replace("₹", "").trim()}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }
                }
            }
        }

        // 2. Cost Category Breakdown
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Itemized Material & Labour Cost",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showRatesDialog = true },
                            modifier = Modifier.size(32.dp).testTag("rates_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Edit Rates", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CostRowItem("Plywood 18mm (${estimation.ply18mmSheets} sheets @ ₹${rates.ply18mmPerSheet.toInt()})", estimation.ply18mmCost, inrFormat)
                    CostRowItem("Plywood 9mm Backing (${estimation.ply9mmSheets} sheets)", estimation.ply9mmCost, inrFormat)
                    CostRowItem("Exterior Laminate (${estimation.laminateOutsideSheets} sheets)", estimation.laminateOutsideCost, inrFormat)
                    CostRowItem("Interior Liner Laminate (${estimation.laminateInsideSheets} sheets)", estimation.laminateInsideCost, inrFormat)
                    CostRowItem("PVC Edge Banding (${estimation.edgeBandingMeters.toInt()}m)", estimation.edgeBandingCost, inrFormat)
                    CostRowItem("Soft-close Hinges (${estimation.hingesCount} pcs)", estimation.hingesCost, inrFormat)
                    CostRowItem("Handles & Drawer Channels (${estimation.handlesCount} handles)", estimation.handlesCost + estimation.drawerChannelsCost, inrFormat)
                    CostRowItem("Adhesive, Screws & Assembly Hardware", estimation.fevicolCost + estimation.screwsHardwareCost, inrFormat)
                    CostRowItem("Carpentry Workshop Labour", estimation.labourCost, inrFormat)
                    CostRowItem("Transportation & Delivery", estimation.transportationCost, inrFormat)
                    CostRowItem("Site Installation & Leveling", estimation.installationCost, inrFormat)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Production Cost Subtotal:", fontWeight = FontWeight.Bold)
                        Text(text = "₹ ${inrFormat.format(estimation.costOfProduction).replace("₹", "").trim()}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Profit Margin & Taxes Levers
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Margin, Taxes & Payment Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Profit Margin Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${Localization.get("profit_margin", language)}: ${estimation.profitMarginPercent.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "+ ₹ ${inrFormat.format(estimation.profitAmount).replace("₹", "").trim()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = estimation.profitMarginPercent.toFloat(),
                        onValueChange = {
                            onUpdateFinancials(it.toDouble(), estimation.gstPercent, estimation.advancePaid)
                        },
                        valueRange = 0f..40f,
                        steps = 39,
                        modifier = Modifier.height(28.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // GST Rate Selector Chips
                    Text(text = Localization.get("gst_tax", language), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.0, 5.0, 12.0, 18.0, 28.0).forEach { rate ->
                            FilterChip(
                                selected = estimation.gstPercent == rate,
                                onClick = { onUpdateFinancials(estimation.profitMarginPercent, rate, estimation.advancePaid) },
                                label = { Text("${rate.toInt()}%", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Advance Payment Text Field
                    OutlinedTextField(
                        value = advanceText,
                        onValueChange = {
                            advanceText = it
                            val adv = it.toDoubleOrNull() ?: 0.0
                            onUpdateFinancials(estimation.profitMarginPercent, estimation.gstPercent, adv)
                        },
                        label = { Text("Advance Payment Received (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("advance_payment_input")
                    )
                }
            }
        }

        // 4. Action Buttons (PDF Quotation & WhatsApp Share)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToQuotation,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("preview_quotation_pdf_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Quotation", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        project?.let { prj ->
                            val pdfFile = PdfReportGenerator.generateQuotationPdf(context, prj, config, cuttingParts, estimation)
                            ShareHelper.shareQuotationViaWhatsApp(context, pdfFile, prj, estimation)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("whatsapp_share_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 13.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit Rates Dialog
    if (showRatesDialog) {
        RatesSettingsDialog(
            currentRates = rates,
            onDismiss = { showRatesDialog = false },
            onSave = {
                onUpdateRates(it)
                showRatesDialog = false
            }
        )
    }
}

@Composable
private fun CostRowItem(label: String, amount: Double, inrFormat: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            text = "₹ ${inrFormat.format(amount).replace("₹", "").trim()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RatesSettingsDialog(
    currentRates: MaterialRates,
    onDismiss: () -> Unit,
    onSave: (MaterialRates) -> Unit
) {
    var ply18 by remember { mutableStateOf(currentRates.ply18mmPerSheet.toInt().toString()) }
    var ply9 by remember { mutableStateOf(currentRates.ply9mmPerSheet.toInt().toString()) }
    var lamOut by remember { mutableStateOf(currentRates.laminateOutsidePerSheet.toInt().toString()) }
    var lamIn by remember { mutableStateOf(currentRates.laminateInsidePerSheet.toInt().toString()) }
    var edgeBanding by remember { mutableStateOf(currentRates.edgeBandingPerMeter.toInt().toString()) }
    var hinge by remember { mutableStateOf(currentRates.hingePerPiece.toInt().toString()) }
    var handle by remember { mutableStateOf(currentRates.handlePerPiece.toInt().toString()) }
    var channel by remember { mutableStateOf(currentRates.drawerChannelPerPair.toInt().toString()) }
    var labour by remember { mutableStateOf(currentRates.labourPerSqFt.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Market Unit Rates (INR)") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = ply18,
                        onValueChange = { ply18 = it },
                        label = { Text("18mm Ply / Sheet (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = ply9,
                        onValueChange = { ply9 = it },
                        label = { Text("9mm Back Ply / Sheet (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = lamOut,
                        onValueChange = { lamOut = it },
                        label = { Text("Exterior Laminate / Sheet (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = lamIn,
                        onValueChange = { lamIn = it },
                        label = { Text("Liner Laminate / Sheet (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = edgeBanding,
                        onValueChange = { edgeBanding = it },
                        label = { Text("Edge Banding / Meter (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = hinge,
                        onValueChange = { hinge = it },
                        label = { Text("Soft-close Hinge / Piece (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = channel,
                        onValueChange = { channel = it },
                        label = { Text("Drawer Channel / Pair (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = labour,
                        onValueChange = { labour = it },
                        label = { Text("Carpentry Labour / Sq.Ft (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = currentRates.copy(
                        ply18mmPerSheet = ply18.toDoubleOrNull() ?: currentRates.ply18mmPerSheet,
                        ply9mmPerSheet = ply9.toDoubleOrNull() ?: currentRates.ply9mmPerSheet,
                        laminateOutsidePerSheet = lamOut.toDoubleOrNull() ?: currentRates.laminateOutsidePerSheet,
                        laminateInsidePerSheet = lamIn.toDoubleOrNull() ?: currentRates.laminateInsidePerSheet,
                        edgeBandingPerMeter = edgeBanding.toDoubleOrNull() ?: currentRates.edgeBandingPerMeter,
                        hingePerPiece = hinge.toDoubleOrNull() ?: currentRates.hingePerPiece,
                        handlePerPiece = handle.toDoubleOrNull() ?: currentRates.handlePerPiece,
                        drawerChannelPerPair = channel.toDoubleOrNull() ?: currentRates.drawerChannelPerPair,
                        labourPerSqFt = labour.toDoubleOrNull() ?: currentRates.labourPerSqFt
                    )
                    onSave(updated)
                }
            ) {
                Text("Save Rates")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
