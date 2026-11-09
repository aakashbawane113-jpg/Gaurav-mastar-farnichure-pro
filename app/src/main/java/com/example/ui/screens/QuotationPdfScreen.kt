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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import com.example.util.ShareHelper
import com.example.viewmodel.Screen
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuotationPdfScreen(
    project: FurnitureProject?,
    config: FurnitureConfig,
    cuttingParts: List<CuttingPart>,
    estimation: Estimation,
    language: AppLanguage,
    onNavigateToEstimation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    val prj = project ?: FurnitureProject(
        customerName = "Valued Customer",
        projectName = "Custom Furniture Design"
    )

    fun getGeneratedPdf(): File {
        return PdfReportGenerator.generateQuotationPdf(context, prj, config, cuttingParts, estimation)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
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
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Professional PDF Quotation Ready",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Includes 3D schematic, dimensions, cutting list, itemized pricing & terms.",
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
                        val pdf = getGeneratedPdf()
                        ShareHelper.shareQuotationViaWhatsApp(context, pdf, prj, estimation)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("whatsapp_direct_share_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Localization.get("share_whatsapp", language), fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val pdf = getGeneratedPdf()
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
                            val pdf = getGeneratedPdf()
                            Toast.makeText(context, "Quotation PDF saved: ${pdf.name}", Toast.LENGTH_LONG).show()
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
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Gaurav Furniture Pro • Design & Quotation",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Q-${prj.id.toString().padStart(4, '0')}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
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
                        color = Color(0xFF1E293B),
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
