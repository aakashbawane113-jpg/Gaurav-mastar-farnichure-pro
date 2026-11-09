package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OptimizationResult
import com.example.model.OptimizedSheet
import com.example.model.StandardSheetSize
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.viewmodel.Screen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetOptimizerScreen(
    optimizationResult: OptimizationResult?,
    selectedSheetSize: StandardSheetSize,
    bladeKerfMm: Int,
    activeSheetIndex: Int,
    language: AppLanguage,
    onSelectSheetSize: (StandardSheetSize) -> Unit,
    onSelectBladeKerf: (Int) -> Unit,
    onSelectSheetIndex: (Int) -> Unit,
    onNavigateToEstimation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var sheetSizeDropdown by remember { mutableStateOf(false) }

    val sheets = optimizationResult?.sheets ?: emptyList()
    val currentSheet = if (sheets.isNotEmpty()) sheets.getOrNull(activeSheetIndex) ?: sheets[0] else null

    val cutColors = remember {
        listOf(
            Color(0xFF0284C7), // Blue
            Color(0xFFD97706), // Amber
            Color(0xFF16A34A), // Green
            Color(0xFF7C3AED), // Purple
            Color(0xFFE11D48), // Rose
            Color(0xFF0D9488)  // Teal
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Controls Header: Sheet Size & Kerf
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Sheet Cutting Optimization Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = sheetSizeDropdown,
                        onExpandedChange = { sheetSizeDropdown = !sheetSizeDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedSheetSize.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Localization.get("sheet_size", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sheetSizeDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = sheetSizeDropdown,
                            onDismissRequest = { sheetSizeDropdown = false }
                        ) {
                            StandardSheetSize.entries.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text(size.label) },
                                    onClick = {
                                        onSelectSheetSize(size)
                                        sheetSizeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Blade Kerf Chips
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Localization.get("blade_kerf", language),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        listOf(2, 3, 4, 5).forEach { kerf ->
                            FilterChip(
                                selected = bladeKerfMm == kerf,
                                onClick = { onSelectBladeKerf(kerf) },
                                label = { Text("${kerf}mm", fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Metrics Summary
        if (optimizationResult != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricPill(
                        title = Localization.get("sheets_required", language),
                        value = "${optimizationResult.totalSheets} pcs",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricPill(
                        title = Localization.get("efficiency_percent", language),
                        value = "${String.format(Locale.US, "%.1f", 100.0 - optimizationResult.averageWastagePercent)}%",
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    MetricPill(
                        title = Localization.get("wastage_percent", language),
                        value = "${String.format(Locale.US, "%.1f", optimizationResult.averageWastagePercent)}%",
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Sheet Pager / Selector
        if (sheets.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sheet ${activeSheetIndex + 1} of ${sheets.size} (${currentSheet?.thicknessMm ?: 18}mm)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        IconButton(
                            onClick = { onSelectSheetIndex((activeSheetIndex - 1).coerceAtLeast(0)) },
                            enabled = activeSheetIndex > 0
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Sheet")
                        }
                        IconButton(
                            onClick = { onSelectSheetIndex((activeSheetIndex + 1).coerceAtMost(sheets.size - 1)) },
                            enabled = activeSheetIndex < sheets.size - 1
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Sheet")
                        }
                    }
                }

                // Sheet tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(sheets) { idx, s ->
                        FilterChip(
                            selected = activeSheetIndex == idx,
                            onClick = { onSelectSheetIndex(idx) },
                            label = { Text("Sheet ${idx + 1} (${s.thicknessMm}mm)") }
                        )
                    }
                }
            }

            // 4. Visual Cutting Diagram Canvas
            item {
                currentSheet?.let { sheet ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth().testTag("sheet_visual_diagram")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "Sheet Layout (${selectedSheetSize.lengthMm} x ${selectedSheetSize.widthMm} mm)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Yield: ${String.format(Locale.US, "%.1f", sheet.efficiencyPercent)}%",
                                    color = Color(0xFFFBBF24),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Interactive 2D Sheet Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(selectedSheetSize.lengthMm.toFloat() / selectedSheetSize.widthMm.toFloat())
                                    .background(Color(0xFF334155))
                                    .border(1.dp, Color(0xFF64748B))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val scaleX = size.width / selectedSheetSize.lengthMm.toFloat()
                                    val scaleY = size.height / selectedSheetSize.widthMm.toFloat()

                                    // Draw Waste / Offcut rects
                                    sheet.wasteRects.forEach { w ->
                                        drawRect(
                                            color = Color(0x33EF4444), // Shaded red for offcut
                                            topLeft = Offset(w.x * scaleX, w.y * scaleY),
                                            size = Size(w.width * scaleX, w.height * scaleY)
                                        )
                                        drawRect(
                                            color = Color(0x66EF4444),
                                            topLeft = Offset(w.x * scaleX, w.y * scaleY),
                                            size = Size(w.width * scaleX, w.height * scaleY),
                                            style = Stroke(1f)
                                        )
                                    }

                                    // Draw Placed Parts
                                    sheet.placedCuts.forEach { cut ->
                                        val c = cutColors[cut.colorIndex % cutColors.size]
                                        val rx = cut.x * scaleX
                                        val ry = cut.y * scaleY
                                        val rw = cut.width * scaleX
                                        val rh = cut.height * scaleY

                                        drawRect(
                                            color = c,
                                            topLeft = Offset(rx, ry),
                                            size = Size(rw, rh)
                                        )
                                        drawRect(
                                            color = Color.White.copy(alpha = 0.8f),
                                            topLeft = Offset(rx, ry),
                                            size = Size(rw, rh),
                                            style = Stroke(1.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Placed Parts on this sheet breakdown
            item {
                currentSheet?.let { sheet ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Cut Parts on this Sheet (${sheet.placedCuts.size} cuts)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            sheet.placedCuts.forEachIndexed { i, cut ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = cutColors[cut.colorIndex % cutColors.size],
                                            modifier = Modifier.size(12.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = cut.partName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        if (cut.rotated) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "↺ 90°", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Text(
                                        text = "${cut.width} x ${cut.height} mm",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (i < sheet.placedCuts.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Navigation to Estimation
        item {
            Button(
                onClick = onNavigateToEstimation,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("goto_estimation_btn")
            ) {
                Icon(Icons.Default.Paid, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Proceed to Cost Estimation & Quotation")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        }
    }
}
