package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CuttingPart
import com.example.model.Estimation
import com.example.model.FurnitureConfig
import com.example.model.FurnitureProject
import com.example.model.OptimizationResult
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import com.example.util.ShareHelper
import java.io.File
import java.text.NumberFormat
import java.util.Locale

enum class PdfDocumentType {
    QUOTATION,
    CUTTING_LIST
}

@Composable
fun QuotationPdfScreen(
    project: FurnitureProject?,
    config: FurnitureConfig,
    cuttingParts: List<CuttingPart>,
    estimation: Estimation,
    language: AppLanguage,
    onNavigateToEstimation: () -> Unit,
    onBack: () -> Unit,
    optimizationResult: OptimizationResult? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    var selectedTab by remember { mutableStateOf(PdfDocumentType.QUOTATION) }

    val prj = project ?: FurnitureProject(
        customerName = "Valued Customer",
        projectName = "Custom Furniture Design"
    )

    fun getActivePdf(): File {
        return when (selectedTab) {
            PdfDocumentType.QUOTATION -> PdfReportGenerator.generateQuotationPdf(context, prj, config, cuttingParts, estimation)
            PdfDocumentType.CUTTING_LIST -> PdfReportGenerator.generateCuttingListPdf(context, prj, config, cuttingParts, optimizationResult)
        }
    }

    val totalPanels = cuttingParts.sumOf { it.quantity }
    val totalAreaSqFt = cuttingParts.sumOf { it.totalAreaSqFt }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Document Type Tabs (Quotation PDF vs Cutting List PDF)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Tab(
                        selected = selectedTab == PdfDocumentType.QUOTATION,
                        onClick = { selectedTab = PdfDocumentType.QUOTATION },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.get("quotation_pdf", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_quotation_pdf")
                    )
                    Tab(
                        selected = selectedTab == PdfDocumentType.CUTTING_LIST,
                        onClick = { selectedTab = PdfDocumentType.CUTTING_LIST },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.get("cutting_list_pdf", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_cutting_list_pdf")
                    )
                }
            }
        }

        // 1. Header Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedTab == PdfDocumentType.QUOTATION) Icons.Default.PictureAsPdf else Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (selectedTab == PdfDocumentType.QUOTATION) "Client Quotation PDF Ready" else "Workshop Cutting List PDF Ready",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedTab == PdfDocumentType.QUOTATION)
                                "Includes 3D schematic, dimensions, material cost, taxes & terms."
                            else
                                "Detailed panel breakdown, dimensions, edge banding & sheet nesting report.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 2. Action Buttons (WhatsApp, Download, Print)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // WhatsApp Primary Share Button
                Button(
                    onClick = {
                        val pdf = getActivePdf()
                        if (selectedTab == PdfDocumentType.QUOTATION) {
                            ShareHelper.shareQuotationViaWhatsApp(context, pdf, prj, estimation)
                        } else {
                            ShareHelper.shareCuttingListViaWhatsApp(context, pdf, prj, totalPanels)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("whatsapp_direct_share_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTab == PdfDocumentType.QUOTATION) "Share Quotation on WhatsApp" else "Share Cutting List on WhatsApp",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val pdf = getActivePdf()
                            ShareHelper.viewOrPrintPdf(context, pdf)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("view_print_pdf_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View / Print", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val pdf = getActivePdf()
                            Toast.makeText(context, "PDF saved: ${pdf.name}", Toast.LENGTH_LONG).show()
                            ShareHelper.viewOrPrintPdf(context, pdf)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("download_pdf_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Document Simulation Paper Preview
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Document Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GAURAV FURNITURE PRO",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF1D4ED8)
                            )
                            Text(
                                text = if (selectedTab == PdfDocumentType.QUOTATION) "Client Quotation & Specifications" else "Workshop Cutting List & Material Schedule",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Surface(
                            color = if (selectedTab == PdfDocumentType.QUOTATION) Color(0xFFFEF3C7) else Color(0xFFDBEAFE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (selectedTab == PdfDocumentType.QUOTATION) "Q-${prj.id.toString().padStart(4, '0')}" else "CUT-${prj.id.toString().padStart(4, '0')}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (selectedTab == PdfDocumentType.QUOTATION) Color(0xFFB45309) else Color(0xFF1D4ED8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Client details
                    Text(text = "CLIENT: ${prj.customerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                    Text(text = "Mobile: ${prj.customerPhone.ifEmpty { "Not provided" }}", fontSize = 11.sp, color = Color(0xFF475569))
                    Text(text = "Site: ${prj.customerAddress.ifEmpty { "Client Location" }}", fontSize = 11.sp, color = Color(0xFF475569))
                    Text(text = "Project: ${prj.projectName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1E293B))

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedTab == PdfDocumentType.QUOTATION) {
                        // Furniture Specs
                        Text(text = "SPECIFICATIONS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                        Text(
                            text = "• Category: ${config.category.englishName}\n• Dimensions: ${config.widthMm}mm (W) x ${config.heightMm}mm (H) x ${config.depthMm}mm (D)\n• Structure: ${config.carcassThicknessMm}mm Board, ${config.backThicknessMm}mm Backing\n• Finish: ${config.finish.displayName}\n• Config: ${config.numberOfDoors} Doors, ${config.shelvesCount} Shelves, ${config.drawersCount} Drawers",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Costing overview
                        Text(text = "ESTIMATION BREAKDOWN", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        PreviewLineItem("Raw Boards & Plywood (${estimation.ply18mmSheets + estimation.ply9mmSheets} sheets)", estimation.totalBoardCost, inrFormat)
                        PreviewLineItem("Laminate & Edge Banding", estimation.totalLaminateCost + estimation.edgeBandingCost, inrFormat)
                        PreviewLineItem("Hardware, Hinges & Channels", estimation.totalHardwareCost, inrFormat)
                        PreviewLineItem("Labour, Fabrication & Installation", estimation.totalServicesCost, inrFormat)
                        PreviewLineItem("GST Tax (${estimation.gstPercent.toInt()}%)", estimation.gstAmount, inrFormat)

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFF1D4ED8),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "FINAL QUOTATION TOTAL:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Text(
                                    text = "₹ ${inrFormat.format(estimation.grandTotal).replace("₹", "").trim()}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Advance: ₹ ${inrFormat.format(estimation.advancePaid).replace("₹", "").trim()}", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(text = "Balance: ₹ ${inrFormat.format(estimation.balanceRemaining).replace("₹", "").trim()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                    } else {
                        // Cutting List simulation preview
                        Text(text = "CUTTING LIST SCHEDULE (${cuttingParts.size} parts, $totalPanels panels)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(6.dp))

                        cuttingParts.take(6).forEach { part ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${part.quantity}x ${part.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${part.lengthMm} x ${part.widthMm} x ${part.thicknessMm} mm",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        if (cuttingParts.size > 6) {
                            Text(
                                text = "... and ${cuttingParts.size - 6} more parts in generated PDF table",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "SHEET NESTING ESTIMATE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1D4ED8))
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val reqSheets = optimizationResult?.totalSheets ?: (estimation.ply18mmSheets + estimation.ply9mmSheets)
                                    val effPercent = optimizationResult?.let { 100.0 - it.averageWastagePercent } ?: 88.5
                                    Text(text = "Required Sheets: $reqSheets", fontSize = 10.sp, color = Color(0xFF334155))
                                    Text(text = "Nesting Efficiency: ${String.format(Locale.US, "%.1f", effPercent)}%", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D4ED8))
                                }
                                Text(text = "Total Panel Area: ${String.format(Locale.US, "%.1f", totalAreaSqFt)} sq.ft", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onNavigateToEstimation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Modify Estimation Values")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PreviewLineItem(label: String, amount: Double, inrFormat: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF475569))
        Text(text = "₹ ${inrFormat.format(amount).replace("₹", "").trim()}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
    }
}
