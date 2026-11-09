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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CuttingPart
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.viewmodel.Screen
import java.util.Locale

@Composable
fun CuttingListScreen(
    cuttingParts: List<CuttingPart>,
    language: AppLanguage,
    onAddPart: (CuttingPart) -> Unit,
    onUpdatePart: (CuttingPart) -> Unit,
    onDeletePart: (String) -> Unit,
    onNavigateToOptimizer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showAddDialog by remember { mutableStateOf(false) }
    var partToEdit by remember { mutableStateOf<CuttingPart?>(null) }

    val totalPanels = cuttingParts.sumOf { it.quantity }
    val totalAreaSqFt = cuttingParts.sumOf { it.totalAreaSqFt }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header stats banner
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = Localization.get("cutting_list", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total Panels: $totalPanels pcs  •  ${String.format(Locale.US, "%.1f", totalAreaSqFt)} sq.ft",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Button(
                            onClick = onNavigateToOptimizer,
                            modifier = Modifier.testTag("optimize_sheets_cta")
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Localization.get("sheet_optimizer", language), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Subtitle & Count
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Panels Specification (${cuttingParts.size} parts)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("add_part_manual_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.get("add_custom_part", language), fontSize = 11.sp)
                    }
                }
            }

            // Cutting Parts list items
            items(cuttingParts, key = { it.id }) { part ->
                CuttingPartCard(
                    part = part,
                    onEdit = { partToEdit = part },
                    onDelete = { onDeletePart(part.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Floating Action Button to Add Part
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_part")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Cutting Part")
        }
    }

    // Add or Edit Part Dialog
    if (showAddDialog || partToEdit != null) {
        PartEditDialog(
            initialPart = partToEdit,
            onDismiss = {
                showAddDialog = false
                partToEdit = null
            },
            onSave = { savedPart ->
                if (partToEdit != null) {
                    onUpdatePart(savedPart)
                } else {
                    onAddPart(savedPart)
                }
                showAddDialog = false
                partToEdit = null
            }
        )
    }
}

@Composable
private fun CuttingPartCard(
    part: CuttingPart,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("part_card_${part.name.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = part.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Qty: ${part.quantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dimensions details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(text = "Length x Width x Thickness", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${part.lengthMm} x ${part.widthMm} x ${part.thicknessMm} mm",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                Column {
                    Text(text = "Material Type", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = part.materialType,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edge: ${part.edgeBanding}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Part", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Part",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartEditDialog(
    initialPart: CuttingPart?,
    onDismiss: () -> Unit,
    onSave: (CuttingPart) -> Unit
) {
    var name by remember { mutableStateOf(initialPart?.name ?: "Custom Panel") }
    var lengthStr by remember { mutableStateOf(initialPart?.lengthMm?.toString() ?: "1200") }
    var widthStr by remember { mutableStateOf(initialPart?.widthMm?.toString() ?: "600") }
    var thicknessStr by remember { mutableStateOf(initialPart?.thicknessMm?.toString() ?: "18") }
    var qtyStr by remember { mutableStateOf(initialPart?.quantity?.toString() ?: "1") }
    var materialType by remember { mutableStateOf(initialPart?.materialType ?: "18mm Plywood") }
    var edgeBanding by remember { mutableStateOf(initialPart?.edgeBanding ?: "2mm PVC (2L + 2W)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialPart != null) "Edit Cutting Part" else "Add New Cutting Part") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Part Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lengthStr,
                        onValueChange = { lengthStr = it },
                        label = { Text("Length (mm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = widthStr,
                        onValueChange = { widthStr = it },
                        label = { Text("Width (mm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = thicknessStr,
                        onValueChange = { thicknessStr = it },
                        label = { Text("Thick (mm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = materialType,
                    onValueChange = { materialType = it },
                    label = { Text("Material (e.g. 18mm Ply)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = edgeBanding,
                    onValueChange = { edgeBanding = it },
                    label = { Text("Edge Banding") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val part = CuttingPart(
                        id = initialPart?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name.ifEmpty { "Panel" },
                        lengthMm = lengthStr.toIntOrNull() ?: 100,
                        widthMm = widthStr.toIntOrNull() ?: 100,
                        thicknessMm = thicknessStr.toIntOrNull() ?: 18,
                        quantity = qtyStr.toIntOrNull() ?: 1,
                        materialType = materialType,
                        edgeBanding = edgeBanding,
                        isCustom = true
                    )
                    onSave(part)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
